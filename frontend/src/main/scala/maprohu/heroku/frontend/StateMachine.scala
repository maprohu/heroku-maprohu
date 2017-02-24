package maprohu.heroku.frontend

import org.scalajs.dom

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class StateMachineParams(
  alert: String => Unit = s => dom.window.alert(s)
)

object StateMachine {

  def create(
    initial: State,
    bufferSize: Int = 1024
  )(implicit
    params: StateMachineParams = StateMachineParams()
  ) = {

    new StateMachine {
      var state = initial
      var processing = Future.successful()
      var queue = Vector.empty[Event]

      def start(event: Event) : Unit = {
        processing =
          state
            .process(event)
            .map({ s =>
              state = s

              queue match {
                case head +: tail =>
                  queue = tail
                  start(head)
                case _ =>
              }
            })
      }

      def handleOverflow(): Unit = {
        params.alert(s"event overflow: ${queue.size}")
      }

      override def process(event: Event) : Unit = {
        if (processing.isCompleted) {
          start(event)
        } else {
          queue :+= event

          if (queue.size > bufferSize) {
            handleOverflow()
          }

        }
      }
    }

  }

}

trait StateMachine {
  def process(event: Event) : Unit
}
