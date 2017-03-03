package maprohu.heroku.frontend

import maprohu.heroku.frontend.TerminalApp.{ClientToServerWrapper, DisplayMessageWrapper}
import maprohu.heroku.frontend.ui.{DefaultDisplayMessage, DisplayMessage, Terminal}
import maprohu.heroku.frontend.ui.Terminal.Command
import maprohu.heroku.shared.{ChatMessage, ClientToServer}
import monix.reactive.Observable
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

  case class TerminalState(
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

  implicit def wrapClient(s: ClientToServer) : ClientToServerWrapper = {
    ClientToServerWrapper(s)
  }

  def nameChangeMessage(name: String) = {
    s"your name is: ${name}"
  }

  def run(
    connection: Observable[ConnectionEvent],
    session: Session
  ) = {
    new RunningApp(connection, session)
  }

}

class RunningApp(
  connection: Observable[ConnectionEvent],
  session: Session
) {
  import TerminalApp._

  val storage =
    dom
      .window
      .localStorage

  val settings = {
    import upickle.default._
    Option(storage.getItem(SettingsKey))
      .map({ s =>
        read[Settings](s)
      })
      .getOrElse(Settings())
  }

  def writeSettings(s: Settings) = {
    import upickle.default._
    storage
      .setItem(
        SettingsKey,
        write(s)
      )

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


  val initialState =
    TerminalState(
      settings = settings
    )

  val stateOut = PublishSubject[Output]()

  val stateFlowCancel =
    Observable
      .merge(
        connection.map(ConnectionEventWrapper.apply),
        commandsSubject.map(CommandWrapper.apply)
      )
      .flatScan(initialState)({ (state, event) =>
        new StateTransition(state, event)(this).process()
      })
      .flatMap(_.out)
      .subscribe(stateOut)

  val messages =
    Observable
      .merge(
        stateOut
          .collect({
            case o : DisplayMessageWrapper => o.message
          }),
        Observable[DisplayMessage](
          nameChangeMessage(settings.name)
        )
      )


  stateOut
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

class StateTransition(
  val state: TerminalApp.TerminalState,
  val event: TerminalApp.Event
)(val runningApp: RunningApp) {
  import runningApp._
  import TerminalApp._

  implicit def st(s: TerminalState) : Future[TerminalState] = Future.successful(s)

  implicit def outputOnly[T](m: T)(implicit ev: T => Observable[Output]) : Future[TerminalState] = {
    state.copy(
      out = ev(m)
    )
  }

  implicit def displayMessage(m: DisplayMessage) : Future[TerminalState] = {
    state.copy(
      out = Observable(
        DisplayMessageWrapper(m)
      )
    )
  }

  implicit def string(s: String) : Future[TerminalState] = {
    DefaultDisplayMessage(s)
  }



  def process() = {

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
            new Commands(this)
              .process(
                command.tail.trim.split("\\s+")
              )
              .getOrElse(
                s"error in expression: ${command}":Future[TerminalState]
              )
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

  }

}

class Commands(
  transition: StateTransition
) {
  import transition._
  import runningApp._
  import TerminalApp._

  def process(line: Array[String]) : Option[Future[TerminalState]] = line match {
    case Array("setname", name) =>
      val s2 = state.settings.copy(name = name)
      writeSettings(s2)
      Some(
        state.copy(
          settings = state.settings.copy(name = name),
          out = nameChangeMessage(name)
        )
      )

    case _ =>
      None

  }

}

object Cmds extends Enumeration {

  case class Cmd(
  ) extends Val {
    def name = toString
  }

  val signon = Cmd()

  val Lookup =
    values
      .toSeq
      .asInstanceOf[Seq[Cmd]]
      .map(v => (v.name, v))
      .toMap
}
