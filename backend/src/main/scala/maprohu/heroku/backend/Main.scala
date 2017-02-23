package maprohu.heroku.backend

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer

import scala.util.Properties

/**
  * Created by pappmar on 23/02/2017.
  */
object Main {

  def main(args: Array[String]): Unit = {
    implicit val actorSystem = ActorSystem()
    implicit val materializer = ActorMaterializer()
    import actorSystem.dispatcher

    import Directives._

    val route = complete("OK3")

    Http()
      .bindAndHandle(
        route,
        "0.0.0.0",
        Properties.envOrElse("PORT", "9881").toInt
      )
      .onComplete({ result =>
        actorSystem.log.info(result.toString)
      })
  }

}
