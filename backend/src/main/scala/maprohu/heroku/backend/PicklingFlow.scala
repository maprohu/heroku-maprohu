package maprohu.heroku.backend

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub}
import akka.util.ByteString
import maprohu.heroku.shared._

import scala.concurrent.duration._

/**
  * Created by pappmar on 23/02/2017.
  */
object PicklingFlow {

  def pickling(
    logic: Flow[ClientToServer, OutputMessage, _]
  )(implicit
    materializer: ActorMaterializer
  ) = {
    import boopickle.Default._
    import materializer.executionContext

    val (broadcastSink, broadcastSource) =
      MergeHub
        .source[ServerToClientMessage]
        .toMat(
          BroadcastHub
            .sink[ServerToClientMessage]
        )(Keep.both)
        .run()

    Flow[Message]
      .mapAsync(1)({
        case bm: BinaryMessage =>
          bm
            .dataStream
            .runReduce(_ ++ _)
        case _ => ???
      })
      .map({ data =>
        Unpickle[ClientToServer].fromBytes(
          data.asByteBuffer
        )
      })
      .via(logic)
      .alsoTo(
        Flow[OutputMessage]
          .collect({
            case e : BroadcastMessage => e.serverToClient
          })
          .to(broadcastSink)
      )
      .collect({
        case e : DirectMessage => e.serverToClient
      })
      .merge(
        broadcastSource,
        eagerComplete = true
      )
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
  serverToClient: ServerToClientMessage
) extends OutputMessage


