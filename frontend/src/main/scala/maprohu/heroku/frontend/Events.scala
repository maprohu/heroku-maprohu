package maprohu.heroku.frontend

import maprohu.heroku.shared.ServerToClientMessage
import org.scalajs.dom.raw.MessageEvent

/**
  * Created by pappmar on 23/02/2017.
  */
object Events {

}

sealed trait Event

sealed trait ConnectionEvent extends Event
case object ConnectionLost extends ConnectionEvent
case object ConnectionEstablished extends ConnectionEvent
case class MessageFromServer(
  message: ServerToClientMessage
) extends ConnectionEvent
