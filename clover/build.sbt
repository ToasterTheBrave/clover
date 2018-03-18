name := "clover"

version := "0.1"

scalaVersion := "2.11.11"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.2.1"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.2.1"
//libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.2.1" % "provided"

// TODO : I'm right here.  I want this (or equivalent) so that I can finish my Util function, but it causes an error
// TODO : Files to fix: Main.scala:57, Util.scala:14, UtilTest.scala:12
//libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.18.0"

libraryDependencies += "com.paulgoldbaum" %% "scala-influxdb-client" % "0.5.2"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.24"

libraryDependencies += "org.scalamock" %% "scalamock" % "4.1.0" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"

logBuffered in Test := false

