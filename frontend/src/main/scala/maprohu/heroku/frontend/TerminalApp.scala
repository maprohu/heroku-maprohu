package maprohu.heroku.frontend

import maprohu.heroku.frontend.ui.{DefaultDisplayMessage, DisplayMessage, Terminal}
import maprohu.heroku.frontend.ui.Terminal.Command
import maprohu.heroku.shared.{ChatMessage, ClientToServer}
import monix.reactive.Observable
import monix.reactive.observers.Subscriber
import monix.reactive.subjects.PublishSubject
import org.scalajs.dom

import scala.concurrent.Future
import monix.execution.Scheduler.Implicits.global

/**
  * Created by pappmar on 03/03/2017.
  */
object TerminalApp {

  val SettingsKey = "maprohu-heroku-chat"

  case class Settings(
    name : String = "unknown"
  )

  case class State(
    settings: Settings,
    out: Observable[Output] = Observable.empty
  )

  sealed trait Event

  case class ConnectionEventWrapper(
    event: ConnectionEvent
  ) extends Event

  case class CommandWrapper(
    command: Command
  ) extends Event

  sealed trait Output
  case class DisplayMessageWrapper(
    message: DisplayMessage
  ) extends Output

  case class ClientToServerWrapper(
    message: ClientToServer
  ) extends Output

  def run(
    connection: Observable[ConnectionEvent],
    session: Session
  ) = {
    val storage =
      dom
        .window
        .localStorage

    def writeSettings(s: Settings) = {
      import upickle.default._
      storage
        .setItem(
          SettingsKey,
          write(s)
        )

    }

    val settings = {
      import upickle.default._
      Option(storage.getItem(SettingsKey))
        .map({ s =>
          read[Settings](s)
        })
        .getOrElse(Settings())
    }

    val commandsSubject = PublishSubject[Command]()

    implicit def singleout[T](s: T)(implicit ev: T => Output) : Observable[Output] = {
      Observable(ev(s))
    }

    implicit def wrapDisplay[T](s: T)(implicit ev: T => DisplayMessage) : DisplayMessageWrapper = {
      DisplayMessageWrapper(ev(s))
    }

    implicit def string2display(s: String) : DisplayMessage = {
      DefaultDisplayMessage(s)
    }

    def nameChangeMessage(name: String) = {
      s"your name is: ${name}"
    }

    val initialState =
      State(
        settings = settings
      )

    val stateFlow =
      Observable
        .merge(
          connection.map(ConnectionEventWrapper.apply),
          commandsSubject.map(CommandWrapper.apply)
        )
        .flatScan(initialState)({ (state, event) =>

          implicit def st(s: State) : Future[State] = Future.successful(s)

          implicit def outputOnly[T](m: T)(implicit ev: T => Observable[Output]) : Future[State] = {
            state.copy(
              out = ev(m)
            )
          }

          implicit def displayMessage(m: DisplayMessage) : Future[State] = {
            state.copy(
              out = Observable(
                DisplayMessageWrapper(m)
              )
            )
          }

          implicit def string(s: String) : Future[State] = {
            DefaultDisplayMessage(s)
          }


          implicit def wrapClient(s: ClientToServer) : ClientToServerWrapper = {
            ClientToServerWrapper(s)
          }

          Observable.fromFuture({
            event match {
              case w : ConnectionEventWrapper =>
                w.event match {
                  case ConnectionEstablished =>
                    "connection established"
                  case ConnectionLost =>
                    "connection lost"
                  case m : MessageFromServer =>
                    m.message match {
                      case cm : ChatMessage =>
                        s"${cm.from} said: ${cm.message}"
                    }
                }
              case w : CommandWrapper =>
                import w._
                if (command.startsWith(";")) {
                  command.tail.trim.split("\\s+") match {
                    case Array("setname", name) =>
                      val s2 = state.settings.copy(name = name)
                      writeSettings(s2)
                      state.copy(
                        settings = state.settings.copy(name = name),
                        out = nameChangeMessage(name)
                      )

                    case _ =>
                      s"error in expression: ${command}"
                  }
                } else {
                  Observable[Output](
                    s"you said: ${command}",
                    ChatMessage(
                      from = state.settings.name,
                      message = command
                    )
                  )
                }
            }
          })
        })
        .flatMap(_.out)


    val messages =
      Observable
        .merge(
          stateFlow
            .collect({
              case o : DisplayMessageWrapper => o.message
            }),
          Observable[DisplayMessage](
            nameChangeMessage(settings.name)
          )
        )


    stateFlow
      .collect({
        case o : ClientToServerWrapper => o.message
      })
      .subscribe(session.send)

    val node = Terminal
      .setup(
        commandsSubject,
        messages
      )

    session.root.main() = node

  }

}
