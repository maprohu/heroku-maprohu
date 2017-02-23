package maprohu.heroku.frontend

import scala.scalajs.js.JSApp
import scala.concurrent.ExecutionContext.global

/**
  * Created by pappmar on 23/02/2017.
  */
object MainApp extends JSApp {
  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {
    for {
      ws <- WebSocketClient.createClient
    } {

      val machine = StateMachine.create(
        initial = Initial
      )

    }
  }
}
