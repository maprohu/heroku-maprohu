package maprohu.heroku.frontend

import maprohu.heroku.frontend.ui.{Root, Terminal, UI}
import maprohu.heroku.shared.ClientToServer
import monix.reactive.{Consumer, Observable}

import scala.scalajs.js.JSApp
import org.scalajs.dom.raw.WebSocket
import rx.Var
import monix.execution.Scheduler.Implicits.global
import monix.reactive.subjects.PublishSubject

/**
  * Created by pappmar on 23/02/2017.
  */
object MainApp extends JSApp {
  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {

    val connection = Var(Option.empty[WebSocket])

    val root = new Root
    UI.setup(root)

    val subject = PublishSubject[ClientToServer]()

    subject
      .consumeWith(
        Consumer.foreach({ msg =>
          import boopickle.Default._
          import scala.scalajs.js.typedarray.TypedArrayBufferOps._

          connection
            .now
            .foreach({ ws =>
              ws.send(
                Pickle(msg).toByteBuffer.arrayBuffer()
              )
            })
        })
      )
      .runAsync

    implicit val session =
      Session(
        send = subject,
        root = root
      )

    val connectionEvents =
      WebSocketClient
        .observable
        .map({
          case o : WsOpen =>
            connection() = Some(o.webSocket)
            ConnectionEstablished
          case WsClose =>
            connection() = None
            ConnectionLost
          case m : WsMessage =>
            MessageFromServer(m.msg)
        })

    TerminalApp.run(
      connectionEvents,
      session
    )

  }
}
