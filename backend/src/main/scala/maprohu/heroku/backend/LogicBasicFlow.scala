package maprohu.heroku.backend

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import maprohu.heroku.shared.Shared.SessionID
import maprohu.heroku.shared._
import monix.execution.atomic.Atomic

import scala.concurrent.Future


object LogicBasicFlow {


  def createLogic() : Flow[FlowWrapper[ClientToServer], OutputMessage, NotUsed] = {
    Flow[FlowWrapper[ClientToServer]]
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

  lazy val Initial : State = new State {
    override def process(clientToServer: InputMessage) = {
      clientToServer match {
        case m : MessageFromClient =>
          m.msg.message match {
            case c : ChatMessage =>
              Future.successful(
                this,
                Source.single(
                  BroadcastMessage(
                    filter = _ != m.msg.flowId,
                    serverToClient = c
                  )
                )
              )
          }
        case Shutdown =>
          this.future
      }
    }
  }

}



