package maprohu.heroku.backend.data

import maprohu.heroku.backend.data.Data.SchemaVersion

/**
  * Created by pappmar on 03/03/2017.
  */
case class User(
  id: Option[Int],
  name: String,
  hash: Array[Byte]
)

case class Prop(
  id: String,
  value: Array[Byte]
)

object PropDefs extends Enumeration {
  import boopickle.Default._

  case class Def[T: Pickler]() extends Val {
    implicit val pickler = implicitly[Pickler[T]]
  }


  val Version = Def[SchemaVersion]()


}


