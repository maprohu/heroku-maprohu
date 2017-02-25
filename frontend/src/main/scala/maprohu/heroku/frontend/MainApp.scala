package maprohu.heroku.frontend

import maprohu.heroku.frontend.ui.{Root, UI}
import monix.reactive.{Consumer, Observable}

import scala.scalajs.js.JSApp
import org.scalajs.dom.raw.WebSocket
import rx.Var
import monix.execution.Scheduler.Implicits.global

/**
  * Created by pappmar on 23/02/2017.
  */
object MainApp extends JSApp {
  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {

    val connection = Var(Option.empty[WebSocket])

    val root = new Root
    UI.setup(root)

    implicit val session =
      Session(
        send = { msg =>
          import boopickle.Default._
          import scala.scalajs.js.typedarray.TypedArrayBufferOps._
          connection
            .now
            .foreach({ ws =>
              ws.send(
                Pickle(msg).toByteBuffer.arrayBuffer()
              )
            })
        },
        root = root
      )

    val states = States.create

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
      .flatScan(states.initial)({ (state, elem) =>
        Observable
          .fromFuture(
            state.process(elem)
          )
      })
      .consumeWith(
        Consumer.complete
      )
      .runAsync


  }
}
