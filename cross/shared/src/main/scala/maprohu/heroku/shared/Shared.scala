package maprohu.heroku.shared

/**
  * Created by pappmar on 23/02/2017.
  */
object Shared {

  val WebsocketPathElement = "websocket"

}


sealed trait ServerToClient
sealed trait ClientToServer
case object KeepAlive extends ServerToClient with ClientToServer



case class UserInfo(
  id: Long,
  nick: Option[String],
  present: Boolean
)

case object GetUsers extends ClientToServer

case class Users(
  data: Seq[UserInfo]
) extends ServerToClient

