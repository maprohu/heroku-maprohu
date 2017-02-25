package maprohu.heroku.frontend


import maprohu.heroku.frontend.State.FN
import maprohu.heroku.shared.Shared.SessionID
import maprohu.heroku.shared.{CreateSession, ResumeSession, SessionCreated}

import scala.concurrent.Future

/**
  * Created by pappmar on 23/02/2017.
  */
object States {
  def create(implicit session: Session) = new States
}

class States(implicit session: Session) {

  def connectionStatus(evt: Event) = evt match {
    case ConnectionEstablished =>
      session.root.connected() = true
    case ConnectionLost =>
      session.root.connected() = false
    case _ =>
  }

  def connection(fn: FN) = State { evt =>
    connectionStatus(evt)

    fn(evt)
  }

  implicit class StateOps(state: State) {
    def future = Future.successful(state)
  }


  def initial : State = connection {
    case ConnectionEstablished =>
      session.send(CreateSession)
      creatingSession.future
    case _ => ???
  }

  def creatingSession : State = connection {
    case m : SessionCreated =>
      sessionCreated(m.id).future
    case ConnectionLost =>
      initial.future
    case _ => ???
  }

  def sessionCreated(id: SessionID) : State = connection {
    case ConnectionLost =>
      connectionLost(id).future
    case _ => ???
  }

  def connectionLost(id: SessionID) : State = connection {
    case ConnectionEstablished =>
      session.send(ResumeSession(id))
      sessionCreated(id).future
    case _ => ???
  }

}

trait State {
  def process(event: Event) : Future[State]
}

object State {
  type FN = Event => Future[State]
  def apply(fn: FN) : State = new State {
    override def process(event: Event): Future[State] = fn(event)
  }
}



