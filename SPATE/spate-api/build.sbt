name := "pythia-api"

version := "1.0"

lazy val `pythia-api` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.4"


libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.spark-project.akka" % "akka-remote_2.10" % "2.3.4-spark",
  "org.apache.hive" % "hive-jdbc" % "2.0.0"
)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")