name := "computer-database-freemarker"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "org.webjars" % "jquery" % "2.1.1",
  "org.webjars" % "bootstrap" % "3.1.1",
  "org.webjars" % "font-awesome" % "4.1.0"
)     

lazy val root = Project("computer-database-freemarker", file("."))
  .enablePlugins(PlayScala)
  .dependsOn(ProjectRef(file("../../code"), "playFreemarker"))


javaOptions in (Test, test) := Seq("-Xmx256m", "-XX:MaxPermSize=128M")
