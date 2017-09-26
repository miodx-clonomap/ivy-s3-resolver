/*
 * Copyright 2004-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ohnosequences.ivy;

import java.util.Optional;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.StorageClass;
import org.apache.ivy.plugins.resolver.RepositoryResolver;
import com.amazonaws.regions.Region;

/**
 * A dependency resolver that looks to an S3 repository to resolve dependencies.
 *
 * @author Ben Hale
 * @author Evdokim Kovach
 */
public class S3Resolver extends RepositoryResolver {

  public S3Resolver(String name, AWSCredentialsProvider credentialsProvider, boolean overwrite, Region region) {
    setName(name);
    setRepository(new S3Repository(credentialsProvider, overwrite, region));
  }

  public S3Resolver(
    String name,
    AWSCredentialsProvider credentialsProvider,
    boolean overwrite,
    Region region,
    CannedAccessControlList acl,
    boolean serverSideEncryption,
    StorageClass storageClass
  ) {
    this(name, credentialsProvider, overwrite, region, Optional.ofNullable(acl), serverSideEncryption, storageClass);
  }

  public S3Resolver(
    String name,
    AWSCredentialsProvider credentialsProvider,
    boolean overwrite,
    Region region,
    Optional<CannedAccessControlList> acl,
    boolean serverSideEncryption,
    StorageClass storageClass
  ) {
    setName(name);
    setRepository(new S3Repository(
      credentialsProvider,
      overwrite,
      region,
      acl,
      serverSideEncryption,
      storageClass
    ));
  }

  public String getTypeName() {
    return "s3";
  }
}
