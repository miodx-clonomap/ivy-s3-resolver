// enablePlugins(JavaOnlySettings)

name := "ivy-s3-resolver"
organization := "com.miodx.sbt.plugins"
version      := "0.9.0"
scalaVersion := "2.10.7"

// bucketSuffix := "era7.com"

// javaVersion := "1.7"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.27",
  "org.apache.ivy" % "ivy" % "2.4.0"
)
