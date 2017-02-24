package maprohu.heroku.frontend

import monix.reactive.Consumer

import scala.scalajs.js.JSApp
import monix.execution.Scheduler.Implicits.global
import org.scalajs.dom
import org.scalajs.dom.raw.WebSocket
import rx.Var

/**
  * Created by pappmar on 23/02/2017.
  */
object MainApp extends JSApp {
  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {
    import rx.Ctx.Owner.Unsafe._

    import scalatags.JsDom.all._

    val d = div.render
    dom.document.body.appendChild(d)

    val connection = Var(Option.empty[WebSocket])

    connection
      .map(_.map(_ => "connected").getOrElse("disconnected"))
      .foreach(s => d.innerHTML = s)

    WebSocketClient
      .observable
      .consumeWith(
        Consumer.foreach({
          case o : WsOpen =>
            connection() = Some(o.webSocket)
          case WsClose =>
            connection() = None
          case _ =>
        })
      )
      .runAsync

  }
}
