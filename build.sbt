name := """sqlmanager"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava,LauncherJarPlugin,PlayEbean)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "mysql" % "mysql-connector-java" % "8.0.13",
  //"mysql" % "mysql-connector-java" % "5.1.18",
  evolutions
)
