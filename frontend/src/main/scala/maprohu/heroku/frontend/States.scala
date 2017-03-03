package maprohu.heroku.frontend


import maprohu.heroku.frontend.ConnectionState.{ConnectionStateInput, FN}
import maprohu.heroku.frontend.State.StateInput
import maprohu.heroku.frontend.pages.{GuestPage, MainPage}
import maprohu.heroku.shared.Shared.SessionID
import maprohu.heroku.shared.{CreateSession, ResumeSession, SessionCreated}

import scala.concurrent.Future

object ConnectionState {

  case class ConnectionStateInput(
    evt: Event,
    session: Session
  )

  type FN = ConnectionStateInput => Future[ConnectionState]

  def apply(fn: FN) : ConnectionState = new ConnectionState {
    override def process(input: ConnectionStateInput): Future[ConnectionState] = fn(input)
  }

  implicit class StateOps(state: ConnectionState) {
    def future : Future[ConnectionState] = Future.successful(state)
  }

  def connectionStatus(evt: Event, session: Session) = evt match {
    case ConnectionEstablished =>
      session.root.connected() = true
    case ConnectionLost =>
      session.root.connected() = false
    case _ =>
  }

  def connection(fn: FN) = ConnectionState { i => import i._
    connectionStatus(evt, session)

    fn(i)
  }



  val initial : ConnectionState = connection { i => import i._
    evt match {
      case ConnectionEstablished =>
        session.send.onNext(CreateSession)
        creatingSession.future
      case _ => ???
    }
  }

  def creatingSession : ConnectionState = connection { i => import i._
    evt match {
      case m : SessionCreated =>
        sessionCreated(m.id).future
      case ConnectionLost =>
        initial.future
      case _ => ???
    }
  }

  def sessionCreated(id: SessionID) : ConnectionState = connection { i => import i._
    evt match {
      case ConnectionLost =>
        connectionLost(id).future
      case _ => ???
    }
  }

  def connectionLost(id: SessionID) : ConnectionState = connection { i => import i._
    evt match {
      case ConnectionEstablished =>
        session.send.onNext(ResumeSession(id))
        sessionCreated(id).future
      case _ => ???
    }
  }

}


trait ConnectionState {
  def process(input: ConnectionStateInput) : Future[ConnectionState]
}

object State {
  case class StateInput(
    event: Event,
    session: Session
  )

  val Initial : State = MainState()
}

trait State {
  def process(input: StateInput) : Future[State]
}

case class MainState(
  connection: ConnectionState = ConnectionState.initial,
  page: MainPage = GuestPage
) extends State {
  override def process(input: StateInput): Future[State] = {
    ???
  }
}


