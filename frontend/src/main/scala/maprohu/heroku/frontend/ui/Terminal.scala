package maprohu.heroku.frontend.ui

import monix.execution.{Ack, Cancelable}
import monix.execution.cancelables.SingleAssignmentCancelable
import monix.reactive.{Consumer, Observable, Observer}
import monix.reactive.OverflowStrategy.Unbounded
import org.scalajs.dom
import org.scalajs.dom.raw.{Event, KeyboardEvent, Node}
import monix.execution.Scheduler.Implicits.global

import scala.scalajs.js

/**
  * Created by pappmar on 03/03/2017.
  */
object Terminal {

  type Command = String



  def setup(
    commandsSubscriber: Observer[Command],
    messagesObservable: Observable[DisplayMessage]
  ) : Node = {
    import scalatags.JsDom.all._

    val messages =
      textarea(
        style := "resize: none;",
        readonly := true,
        flexGrow := 1
      ).render

    def addMessage(msg: String) = {
      messages.value = messages.value + s"\n$msg"
      messages.scrollTop = messages.scrollHeight
    }

    val prompt =
      textarea(

        style := "resize: none;",
        overflow.hidden,
        rows := 1,
        padding := 5.px,
        width := 100.pct,
        flexGrow := 0,
        autofocus

      ).render

    def updatePrompt() = {
      prompt.rows = 1
      var lastClientHeight = 0
      while (prompt.scrollHeight > prompt.clientHeight && prompt.clientHeight != lastClientHeight) {
        lastClientHeight = prompt.clientHeight
        prompt.rows = prompt.rows + 1
      }
    }

    dom
      .window
      .addEventListener[Event](
        "resize",
        { _ => updatePrompt() }
      )

    prompt
      .addEventListener[KeyboardEvent](
        "keydown",
        { e =>
          if (e.keyCode == 9) { // tab
            e.preventDefault()
          }
        }
      )

    val commands =
      Observable
        .create[String](Unbounded) { subscriber =>
          val c = SingleAssignmentCancelable()

          val listener : js.Function1[Event, _] = { _ =>

            val cmds =
              prompt
                .value
                .split("\n", -1)

            prompt.value = cmds.last

            updatePrompt()

            subscriber.onNextAll(
              cmds.init
            )
          }

          prompt
            .addEventListener[Event](
              "input",
              listener
            )

          c := Cancelable(() => prompt.removeEventListener("input", listener))
        }

    commands
      .subscribe(
        commandsSubscriber
      )

    messagesObservable
      .consumeWith(
        Consumer.foreach({
          case m : DefaultDisplayMessage =>
            addMessage(m.message)
        })
      )
      .runAsync

    div(
      display.flex,
      flexDirection.column,
      height := 100.pct,
      width := 100.pct,

      messages,
      prompt
    ).render
  }


}

sealed trait DisplayMessage
case class DefaultDisplayMessage(
  message: String
) extends DisplayMessage
