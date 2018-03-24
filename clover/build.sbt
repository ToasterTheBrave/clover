name := "clover"

version := "0.1"

scalaVersion := "2.11.11"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.2.1"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.2.1"

libraryDependencies += "com.paulgoldbaum" %% "scala-influxdb-client" % "0.5.2"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.24"

libraryDependencies += "org.mockito" % "mockito-core" % "2.16.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"

logBuffered in Test := false
