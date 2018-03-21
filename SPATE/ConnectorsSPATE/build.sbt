name := "MTNConnectors"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.apache.tika" % "tika-app" % "1.12",
  "org.apache.hive" % "hive-jdbc" % "2.0.0",
  "org.apache.hadoop" % "hadoop-client" % "2.5.2",
  "com.decodified" % "scala-ssh_2.10" % "0.7.0"
)
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.6.2"

libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.6.2"

libraryDependencies += "org.roaringbitmap" % "RoaringBitmap" % "0.6.27"

libraryDependencies ++= Seq(
  "org.bouncycastle" % "bcprov-jdk16" % "1.46",
  "com.jcraft" % "jzlib" % "1.1.3"
)