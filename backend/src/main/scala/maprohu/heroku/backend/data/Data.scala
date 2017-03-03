package maprohu.heroku.backend.data

import java.nio.ByteBuffer

import akka.Done
import boopickle.Pickler
import com.typesafe.scalalogging.StrictLogging
import maprohu.heroku.backend.data.Data.SchemaVersion
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

/**
  * Created by pappmar on 03/03/2017.
  */
trait Data extends JdbcProfile with StrictLogging {

  val api : API
  import api._

  val db : backend.DatabaseDef

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Unique)
    def hash = column[Array[Byte]]("hash")
    def * = (id.?, name, hash) <> (User.tupled, User.unapply)
  }
  val users = TableQuery[Users]

  class Props(tag: Tag) extends Table[Prop](tag, "props") {
    def id = column[String]("id", O.PrimaryKey)
    def value = column[Array[Byte]]("value")
    def * = (id, value) <> (Prop.tupled, Prop.unapply)
  }
  val props = TableQuery[Props]

  def schemaVersion(implicit executionContext: ExecutionContext): Future[Option[SchemaVersion]] = {
    readProp(PropDefs.Version)
      .recover({
        case NonFatal(_) => None
      })
  }

  def readProp[T](prop: PropDefs.Def[T])(implicit executionContext: ExecutionContext) = {
    db
      .run(
        props
          .filter(_.id === prop.toString)
          .map(_.value)
          .result
      )
      .map({ opt =>
        opt
          .headOption
          .map({ v =>
            import boopickle.Default._
            import prop.pickler
            Unpickle[T].fromBytes(
              ByteBuffer.wrap(v)
            )
          })
      })
  }

  def writePropIO[T](prop: PropDefs.Def[T], value: T) = {
      props
        .insertOrUpdate({
          import boopickle.Default._
          import prop.pickler
          val bb = Pickle[T](value).toByteBuffer
          val arr = Array.ofDim[Byte](bb.remaining())
          bb.get(arr)

          Prop(
            id = prop.toString(),
            value = arr
          )
        })
  }

  def writeProp[T: Pickler](prop: PropDefs.Def[T], value: T)(implicit executionContext: ExecutionContext) : Future[_] = {
    db
      .run(
        writePropIO(prop, value)
      )
  }

  def init()(implicit executionContext: ExecutionContext) : Future[_] = {
    schemaVersion
      .flatMap({ v =>
        updateFrom(v)
      })
  }

  def updateFrom(version: Option[SchemaVersion])(implicit executionContext: ExecutionContext) : Future[_] = {
    version match {
      case None =>
        logger.info("Creating schema from scratch")

        db.run(
          DBIO.seq(
            (props.schema ++ users.schema).create,
            writePropIO(PropDefs.Version, 0L)
          )
        )

      case Some(0L) =>
        logger.info(s"Schema up to date: ${version}")
        Future.successful(Done)

      case _ => ???
    }

  }

}

object Data {
  type SchemaVersion = Long
}

