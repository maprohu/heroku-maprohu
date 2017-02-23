import maprohu.heroku.backend.{Main, MainParameters, Page}

/**
  * Created by pappmar on 23/02/2017.
  */
object RunMain {

  def main(args: Array[String]): Unit = {
    Main
      .run(
        MainParameters(
          port = 9981,
          resourcesRoute = Page.createRouteFastOpt()
        )
      )
  }

}
