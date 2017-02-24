package maprohu.heroku.shared

/**
  * Created by pappmar on 23/02/2017.
  */
object Shared {

  val WebsocketPathElement = "websocket"

}


sealed trait ClientToServer
sealed trait ServerToClient

sealed trait ServerToClientMessage
case class ServerToClientMessageContainer(
  message: ServerToClientMessage
) extends ServerToClient

case class HelloFromServer() extends ServerToClientMessage
case object KeepAlive extends ServerToClient with ClientToServer




//case class UserInfo(
//  id: Long,
//  nick: Option[String],
//  present: Boolean
//)
//
//case object GetUsers extends ClientToServer
//
//case class Users(
//  data: Seq[UserInfo]
//) extends ServerToClient

