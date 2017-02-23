package maprohu.heroku.frontend

import scala.concurrent.Future

/**
  * Created by pappmar on 23/02/2017.
  */
object States {
  def create(implicit session: Session) = new States
}

class States(implicit session: Session) {

  def initial : State = {
    State(
      _ => Future.successful(initial)
    )
  }

}

case class State(
  process: Event => Future[State]
)



