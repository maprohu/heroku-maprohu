val appName = "heroku-maprohu"

val commonSettings =
  Seq(
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.12.1",
    crossPaths := false
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
      name := appName,
      herokuAppName in Compile := "maprohu",
      mainClass in Compile := Some("maprohu.heroku.backend.Main")
    )
    .aggregate(backend, frontend, crossJS, crossJVM)
    .dependsOn(backend)


lazy val backend =
  project
    .settings(
      commonSettings,
      libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.3",
      libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.6.3"
    )
    .dependsOn(crossJVM)

lazy val cross =
  crossProject
    .settings(
      commonSettings
    )

lazy val crossJS = cross.js
lazy val crossJVM = cross.jvm

lazy val frontend =
  project
    .dependsOn(crossJS)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      commonSettings,
      persistLauncher in Compile := true
    )


