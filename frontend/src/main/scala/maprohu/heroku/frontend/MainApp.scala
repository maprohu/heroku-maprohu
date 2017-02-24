package maprohu.heroku.frontend

import monix.reactive.Consumer

import scala.scalajs.js.JSApp
import monix.execution.Scheduler.Implicits.global

/**
  * Created by pappmar on 23/02/2017.
  */
object MainApp extends JSApp {
  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {

    WebSocketClient
      .observable
      .consumeWith(
        Consumer.foreach(println)
      )
      .runAsync

  }
}
