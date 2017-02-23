package maprohu.heroku.backend

import akka.http.scaladsl.server.Route

/**
  * Created by pappmar on 23/02/2017.
  */
object Page {

  val DepsJs = "deps.js"
  val MainJS = "main.js"
  val LaunchJS = "launch.js"

  def htmlString(
    scripts: Seq[String]
  ) : String = {
    import scalatags.Text.all._

    val doc =
      html(
        body(
          scripts.map({ s =>
            script(
              `type` := "text/javascript",
              src := s
            )
          })
        )
      )

    doc.render
  }

  case class JsNaming(
    dirPrefix: String = "frontend/target",
    jsPrefix: String = "frontend-"
  )


  def createRouteOpt(
    dir: String = ""
  )(implicit
    naming: JsNaming = JsNaming()
  ) : Route = {
    createRoute(
      dir = dir,
      deps = "jsdeps-min",
      main = "opt"
    )
  }

  def createRouteFastOpt(
    dir: String = ""
  )(implicit
    naming: JsNaming = JsNaming()
  ) : Route = {
    createRoute(
      dir = dir,
      deps = "jsdeps",
      main = "fastopt"
    )
  }

  def createRoute(
    dir: String,
    deps: String,
    main: String,
    launch: String = "launcher"
  )(implicit
    naming: JsNaming = JsNaming()
  ) : Route = {
    import akka.http.scaladsl.server.Directives._

    def single(
      pathString: String,
      file: String
    ) = {
      path(pathString) {
        getFromFile(s"${naming.dirPrefix}${dir}/${file}")
      }
    }

    def withMap(
      pathString: String,
      jsBase: String
    ) = {
      val file = s"${naming.jsPrefix}${jsBase}.js"

      single(
        pathString,
        file
      ) ~
      single(
        s"${pathString}.map",
        s"${file}.map"
      )
    }

    withMap(
      DepsJs,
      deps
    ) ~
    withMap(
      MainJS,
      main
    ) ~
    withMap(
      LaunchJS,
      launch
    )
  }

}
