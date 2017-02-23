package maprohu.heroku.frontend

import scala.scalajs.js.JSApp

/**
  * Created by pappmar on 23/02/2017.
  */
object MainApp extends JSApp {
  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {
    println(
      maprohu.heroku.shared.Shared.Value
    )
  }
}
