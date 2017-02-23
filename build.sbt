val appName = "heroku-maprohu"

val commonSettings =
  Seq(
    version := "1.0-SNAPSHOT",
    crossPaths := false,
    herokuAppName in Compile := "maprohu"
  )

lazy val root =
  Project
    .apply(
      appName,
      file(".")
    )
    .enablePlugins(JavaAppPackaging)
    .settings(
      commonSettings,
      scalaVersion := "2.12.1",
      name := appName,
      mainClass in Compile := Some("maprohu.heroku.backend.Main")
    )
    .aggregate(backend)
    .dependsOn(backend)


lazy val backend =
  project
    .settings(
      commonSettings,
      scalaVersion := "2.12.1",
      libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.3"
    )

