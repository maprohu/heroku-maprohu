package maprohu.heroku.frontend

import maprohu.heroku.frontend.ui.Root
import maprohu.heroku.shared.ClientToServer
import monix.execution.Cancelable
import monix.execution.cancelables.{BooleanCancelable, MultiAssignmentCancelable, SerialCancelable, SingleAssignmentCancelable}
import monix.reactive.OverflowStrategy.Unbounded
import monix.reactive.{MulticastStrategy, Observable, Observer}
import org.scalajs.dom
import monix.execution.Scheduler.Implicits.global

/**
  * Created by pappmar on 23/02/2017.
  */
case class Session(
  send: Observer[ClientToServer],
  root: Root
)

//object Session {
//  def connections : Observable[Option[WebSocketClient]] = {
//    Observable.create(Unbounded) { subscriber =>
//
//      val cancelable = MultiAssignmentCancelable()
//
//      def connected(ws: WebSocketClient) = {
//        ws
//          .lost
//          .runAsync
//          .onComplete({ _ =>
//            if (!cancelable.isCanceled) {
//              subscriber.onNext(None)
//              cancelable := Cancelable.empty
//              open()
//            }
//          })
//
//        subscriber.onNext(Some(ws))
//        cancelable := Cancelable(() => ws.closeConnection())
//      }
//
//      def open() : Unit = {
//        if (!cancelable.isCanceled) {
//          val ws = new WebSocketClient
//          ws
//            .opened
//            .runAsync
//            .onComplete({ res =>
//              res
//                .map({ _ =>
//                  connected(ws)
//                })
//                .getOrElse({
//                  if (!cancelable.isCanceled) {
//                    dom.window.setTimeout(
//                      { () =>
//                        open()
//                      },
//                      1000
//                    )
//                  }
//                })
//            })
//        }
//      }
//
//      open()
//
//      cancelable
//    }
//  }
//
//  def create : (Session, Observable[ConnectionEvent]) = {
//    var ws = Option.empty[WebSocketClient]
//
//    val obs =
//      connections
//        .map({ evt =>
//          ws = evt
//
//          evt
//            .map(_ => ConnectionEstablished)
//            .getOrElse(ConnectionLost)
//        })
//
//    val session =
//      Session(
//        send = { msg =>
//          ws.foreach({ w =>
//            import boopickle.Default._
//            import scala.scalajs.js.typedarray.TypedArrayBufferOps._
//            val ab = Pickle.intoBytes(msg).arrayBuffer()
//            w.send(ab)
//          })
//        }
//      )
//
//    (session, obs)
//  }
//}

