package maprohu.heroku.backend

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub}
import akka.util.ByteString
import maprohu.heroku.backend.PicklingFlow.FlowID
import maprohu.heroku.shared._
import monix.execution.atomic.Atomic

import scala.concurrent.duration._

/**
  * Created by pappmar on 23/02/2017.
  */
object PicklingFlow {

  type FlowID = Long

  val flowIds = Atomic(0L)

  def pickling(
    logic: Flow[FlowWrapper[ClientToServer], OutputMessage, _]
  )(implicit
    materializer: ActorMaterializer
  ) = {
    import boopickle.Default._
    import materializer.executionContext

    val (broadcastSink, broadcastSource) =
      MergeHub
        .source[BroadcastMessage]
        .toMat(
          BroadcastHub
            .sink[BroadcastMessage]
        )(Keep.both)
        .run()

    Flow[Message]
      .prefixAndTail(0)
      .flatMapConcat({
        case (_, source) =>
          val flowId = flowIds.getAndIncrement()

          source
            .mapAsync(1)({
              case bm: BinaryMessage =>
                bm
                  .dataStream
                  .runReduce(_ ++ _)
              case _ => ???
            })
            .map({ data =>
              FlowWrapper(
                flowId = flowId,
                message =
                  Unpickle[ClientToServer].fromBytes(
                    data.asByteBuffer
                  )
              )
            })
            .via(logic)
            .alsoTo(
              Flow[OutputMessage]
                .collect({
                  case e : BroadcastMessage => e
                })
                .to(broadcastSink)
            )
            .collect({
              case e : DirectMessage => e.serverToClient
            })
            .merge(
              broadcastSource
                .collect({
                  case m if m.filter(flowId) => m.serverToClient
                }),
              eagerComplete = true
            )

      })
      .map(ServerToClientMessageContainer.apply)
      .keepAlive(
        15.seconds,
        () => KeepAlive
      )
      .map({ m =>
        BinaryMessage(
          ByteString(
            Pickle.intoBytes[ServerToClient](m)
          )
        )
      })
      .watchTermination()((_, done) => done.onComplete(println) )
  }



}

sealed trait OutputMessage

case class DirectMessage(
  serverToClient: ServerToClientMessage
) extends OutputMessage

case class BroadcastMessage(
  filter: FlowID => Boolean,
  serverToClient: ServerToClientMessage
) extends OutputMessage



case class FlowWrapper[T](
  flowId: FlowID,
  message: T
) {
  def map[R](fn: T => R) =
    FlowWrapper(
      flowId = flowId,
      message = fn(message)
    )
}