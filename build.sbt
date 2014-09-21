name := "play2-freemarker-project"

version := "1.0"

lazy val playFreemarker = ProjectRef(file("code"), "playFreemarker")

lazy val computerDatabase = ProjectRef(file("samples/computer-database"), "computer-database-freemarker")

parallelExecution in Test := false

lazy val root = project.in(file(".")).aggregate(playFreemarker,computerDatabase)

crossScalaVersions := Seq("2.10.4", "2.11.2")