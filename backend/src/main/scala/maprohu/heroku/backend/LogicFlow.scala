package maprohu.heroku.backend

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import maprohu.heroku.shared.Shared.SessionID
import maprohu.heroku.shared._
import monix.execution.atomic.Atomic

import scala.concurrent.Future

/**
  * Created by pappmar on 23/02/2017.
  */
sealed trait InputMessage
case class MessageFromClient(msg: ClientToServer) extends InputMessage
case object Shutdown extends InputMessage

object LogicFlow {

  val sessionIds = Atomic(0L)

  case class Session(
    data: SessionData
  )


  val sessions = Atomic(Map.empty[SessionID, Session])

  def createLogic() : Flow[ClientToServer, OutputMessage, NotUsed] = {
    Flow[ClientToServer]
      .map(MessageFromClient.apply)
      .concat(Source.single(Shutdown))
      .scanAsync[(State, Source[OutputMessage, _])]((Initial, Source.empty[OutputMessage]))({ (acc, msg) =>
        val (state, _) = acc
        state.process(msg)
      })
      .flatMapConcat(_._2)
  }

  implicit class Ops1(elem: (State, ServerToClientMessage)) {
    def future = Future.successful((elem._1, Source.single(DirectMessage(elem._2))))
  }
  implicit class Ops2(elem: State) {
    def future = Future.successful((elem, Source.empty))
  }

  val Initial : State = State {
    case m : MessageFromClient =>
      def create = {
        val id = sessionIds.getAndIncrement()
        sessions.transform(_.updated(id, Session(SessionData(id, true))))
        (session(id), SessionCreated(id)).future
      }
      m.msg match {
        case CreateSession =>
          create
        case m : ResumeSession =>
          sessions
            .get
            .get(m.id)
            .map({ s =>
              session(m.id).future
            })
            .getOrElse({
              create
            })
      }
    case Shutdown =>
      shutdown.future

  }

  def session(id: SessionID) : State = State {
    case _ =>
      session(id).future
  }

  def shutdown : State = State {
    case _ => shutdown.future
  }



}

trait State {
  def process(clientToServer: InputMessage) : Future[(State, Source[OutputMessage, _])]
}
object State {
  type FN = InputMessage => Future[(State, Source[OutputMessage, _])]
  def apply(fn: FN) : State = new State {
    override def process(clientToServer: InputMessage) = fn(clientToServer)
  }
}
