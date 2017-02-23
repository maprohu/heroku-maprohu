package maprohu.heroku.frontend

import maprohu.heroku.shared.Shared
import monix.execution.{Ack, Cancelable}
import monix.execution.cancelables.{CompositeCancelable, SingleAssignmentCancelable}
import monix.reactive.Observable
import monix.reactive.OverflowStrategy.Unbounded
import org.scalajs.dom
import org.scalajs.dom.raw.{CloseEvent, ErrorEvent, MessageEvent, WebSocket}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.typedarray.ArrayBuffer
import scala.util.{Failure, Success, Try}

/**
  * Created by pappmar on 23/02/2017.
  */
object WebSocketClient {

  val getWebsocketUri: String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    s"$wsProtocol://${dom.document.location.host}/${Shared.WebsocketPathElement}"
  }

  def createWebSocket() = {
    val ws = new WebSocket(getWebsocketUri)
    ws.binaryType = "arraybuffer"
    ws
  }

  def createClient : Future[WebSocketClient] = {

    val promise = Promise[WebSocketClient]()

    def attempt(): Unit = {
      val ws = createWebSocket()

      ws.onopen = { (e: org.scalajs.dom.raw.Event) =>
        promise.success(new WebSocketClient)
      }

      lazy val retryOnce = {
        dom.window.setTimeout(
          { () =>
            attempt()
          },
          1000
        )
      }

      ws.onerror = { (e:ErrorEvent) =>
        dom.console.warn(e)
        retryOnce
      }

      ws.onclose = { (e:CloseEvent) =>
        retryOnce
      }

    }

    attempt()

    promise.future
  }


}

class WebSocketClient {
  val websocket = WebSocketClient.createWebSocket()

  private def listen[T <: org.scalajs.dom.raw.Event](evt: String) : Observable[T] = {
    MonixTools.listen(websocket, evt)
  }

  val open : Observable[org.scalajs.dom.raw.Event] = listen("open")
  val close : Observable[CloseEvent] = listen("close")
  val error : Observable[ErrorEvent] = listen("error")
  val message : Observable[MessageEvent] = listen("message")

  lazy val lost = {
    Observable
      .merge(
        close,
        error
      )
      .map(_ => ())
      .firstL
  }

  lazy val opened = {
    Observable
      .merge(
        open.map(_ => Success()),
        close.map(e => Failure(new Throwable(e.reason))),
        error.map(e => Failure(new Throwable(e.message)))
      )
      .firstL
  }

  def closeConnection(): Unit = {
    websocket.close()
  }

  def send(ab: ArrayBuffer) = {
    websocket.send(ab)
  }
}



