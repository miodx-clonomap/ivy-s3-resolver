package ohnosequences.ivy

import java.util.Date

import better.files._
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{ListObjectsRequest, ObjectListing, ObjectMetadata, S3ObjectSummary}
import com.amazonaws.util.StringInputStream
import com.amazonaws.util.StringUtils.UTF8
import org.apache.ivy.plugins.repository.Resource
import org.scalamock.scalatest.MockFactory
import org.scalatest._

import scala.collection.convert.WrapAsJava._
import scala.util.Random

/**
 * @author <a href="michael@ahlers.consulting">Michael Ahlers</a>
 */
class S3RepositorySpec extends WordSpec with Matchers with Inside
  with MockFactory {

  "Resource factory" must {
    s"return ${classOf[S3Resource].getName} instance" which {
      "is cached with memoization" in {
        val bucketName = "bucketName"
        val objectKey = Random.nextString(5)
        val source = s"s3://$bucketName/$objectKey"

        val client = mock[AmazonS3]

        /* Overlaps somewhat with tests for S3Resource itself because of a non-trivial constructor. */
        (client.getObjectMetadata(_: String, _: String)).expects(bucketName, objectKey).once() returns {
          val metadata = mock[ObjectMetadata]
          (metadata.getContentLength _).expects().once().returns(0)
          (metadata.getLastModified _).expects().once().returns(new Date)
          metadata
        }

        val repository = new S3MockableRepository(client)

        inside(repository.getResource(source)) {
          case resource: S3Resource =>
            repository.getResource(source) should be theSameInstanceAs resource
        }
      }
    }
  }

  /*
   * TODO: Test transfer initiated signal and progress listener.
   * As of this writing, only ensure the getResource method is called and bytes it provides are written to the destination, which produces a Resource instance that's tested independently with correct URI handling.
   */
  "Get source" must {
    /** Work-around for paulbutcher/ScalaMock#114. */
    abstract class MockableResource extends Resource {
      override def clone(cloneName: String): Resource = ???
    }

    s"use factory-provided ${classOf[Resource].getName}" in {
      val resource = mock[MockableResource]
      val repository = new S3MockableRepository(null) {
        override def getResource(source: String) = resource
      }

      val content = Random.nextString(100)

      (resource.getName _).expects().once().returns(Random.nextString(10))
      (resource.getContentLength _).expects().atLeastOnce().returns(content.getBytes(UTF8).length)
      (resource.openStream _).expects().once().returns(new StringInputStream(content))

      val destination = File.newTemporaryFile()
      repository.get("", destination.toJava)

      destination.contentAsString should be(content)
    }
  }

  /* Verifies listing normalizes the object key, and marshals results from the client correctly. */
  /* TODO: Verify correct iteration over subsequent markers. */
  "List parent" when {

    val scenarios =
      ("s3://bucket/p0/p1", ("bucket", "p0/p1")) ::
        ("s3://bucket/p0//p1", ("bucket", "p0/p1")) ::
        Nil

    scenarios foreach { case (given, (bucketName, objectKey)) =>
      s"""given "$given"""" must {
        "request objects" in {
          val client = mock[AmazonS3]

          val requestPredicate = { request: ListObjectsRequest =>
            bucketName == request.getBucketName &&
              objectKey == request.getPrefix
          }

          val directories =
            objectKey + "/ivys" ::
              objectKey + "/jars" ::
              Nil

          val files =
            for {
              directory <- directories
              file <- "fileA" :: "fileB" :: Nil
            } yield directory + "/" + file

          (client.listObjects(_: ListObjectsRequest)).expects(where(requestPredicate)).once() returns {
            val listing = mock[ObjectListing]
            (listing.getCommonPrefixes _).expects().once().returns(directories)
            (listing.getObjectSummaries _).expects().once() returns {
              files map { file =>
                val summary = mock[S3ObjectSummary]
                (summary.getKey _).expects().once().returns(file)
                summary
              }
            }
            (listing.getNextMarker _).expects().once().returns(null)
            listing
          }

          val repository = new S3MockableRepository(client)
          repository.list(given) should contain theSameElementsAs directories ++ files
        }
      }
    }

  }

}
