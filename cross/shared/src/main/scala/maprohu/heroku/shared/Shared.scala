package maprohu.heroku.shared

import maprohu.heroku.shared.Shared.SessionID

/**
  * Created by pappmar on 23/02/2017.
  */
object Shared {

  val WebsocketPathElement = "websocket"

  type SessionID = Long

}


sealed trait ClientToServer
sealed trait ServerToClient

sealed trait ServerToClientMessage
case class ServerToClientMessageContainer(
  message: ServerToClientMessage
) extends ServerToClient

case object CreateSession extends ClientToServer
case class ResumeSession(id: SessionID) extends ClientToServer
case class SessionCreated(id: SessionID) extends ServerToClientMessage
case object KeepAlive extends ServerToClient

case object GetSessionsList extends ClientToServer
case class SessionsList(
  sessions: Seq[SessionData]
) extends ServerToClientMessage


case class SessionData(
  id: SessionID,
  connected: Boolean
)

case class ChatMessage(
  from: String,
  message: String
) extends ClientToServer with ServerToClientMessage

case class Signup(
  name: String,
  password: String
) extends ClientToServer





