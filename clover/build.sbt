lazy val root = (project in file("."))
  .settings(
    name := "clover",
    version := "0.1",
    scalaVersion := "2.11.11"
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "2.3.0",

  "org.apache.spark" %% "spark-mllib" % "2.3.0",

  "mysql" % "mysql-connector-java" % "5.1.24",

  "org.mockito" % "mockito-core" % "2.16.0" % "test",

  "org.scalatest" %% "scalatest" % "3.0.4" % "test"

)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

logBuffered in Test := false

