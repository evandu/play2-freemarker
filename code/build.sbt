name := "play-freemarker"

licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

homepage := Some(url("https://github.com/playframework/play2-freemarker"))

version := "0.1-SNAPSHOT"

organization := "com.typesafe.play"

scalaVersion := "2.10.4"

crossScalaVersions := Seq("2.10.4", "2.11.1")

resolvers += Classpaths.sbtPluginReleases

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

scalacOptions += "-feature"

scalacOptions += "-deprecation"

parallelExecution in Test := false

libraryDependencies ++= {
  val playVersion = "2.3.2"
  Seq(
    "com.typesafe.play" %% "play" % playVersion,
    "com.typesafe.play" %% "play-jdbc" % playVersion,
    "org.freemarker" % "freemarker" % "2.3.20",
    "com.typesafe.play" %% "play-test" % playVersion % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test")
}

val playFreemarker = project.in(file("."))
