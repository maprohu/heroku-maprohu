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
      mainClass in Compile := Some("maprohu.heroku.backend.Main"),
      stage in Universal := {
        val x = (fullOptJS in Compile in frontend).value
        (stage in Universal).value
      }
    )
    .aggregate(backend, frontend, crossJS, crossJVM)
    .dependsOn(backend)


lazy val backend =
  project
    .settings(
      commonSettings,
      libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.3",
      libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.6.3",
      libraryDependencies += "io.suzaku" %% "boopickle" % "1.2.6",
      libraryDependencies += "io.monix" %% "monix-reactive" % "2.2.2",
      libraryDependencies += "com.typesafe.slick" %% "slick" % "3.2.0",
      libraryDependencies += "org.postgresql" % "postgresql" % "42.0.0.jre7",
      libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.1",
      libraryDependencies += "com.h2database" % "h2" % "1.4.193" % Test
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
      persistLauncher in Compile := true,
      libraryDependencies += "io.suzaku" %%% "boopickle" % "1.2.6",
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1",
      libraryDependencies += "io.monix" %%% "monix-reactive" % "2.2.2",
      libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.6.3",
      libraryDependencies += "com.lihaoyi" %%% "scalarx" % "0.3.2",
      libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.4.4"
    )


