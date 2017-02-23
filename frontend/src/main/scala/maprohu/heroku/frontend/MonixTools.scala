package maprohu.heroku.frontend

import monix.execution.{Ack, Cancelable}
import monix.execution.cancelables.SingleAssignmentCancelable
import monix.reactive.Observable
import monix.reactive.OverflowStrategy.Unbounded
import org.scalajs.dom.EventTarget
import monix.execution.Scheduler.Implicits.global
import scala.scalajs.js

/**
  * Created by pappmar on 23/02/2017.
  */
object MonixTools {

  def listen[T <: org.scalajs.dom.raw.Event](target: EventTarget, evt : String) : Observable[T] = {
    Observable.create(Unbounded) { subscriber =>
      val c = SingleAssignmentCancelable()

      val f : js.Function1[T, Ack] = (e: T) =>
        subscriber
          .onNext(e)
          .syncOnStopOrFailure(_ => c.cancel())

      target.addEventListener[T](evt, f)

      c := Cancelable(() => target.removeEventListener(evt, f))
    }
  }
}
