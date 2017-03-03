package maprohu.heroku.backend

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes, Uri}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import maprohu.heroku.shared.Shared

import scala.util.Properties

case class MainParameters(
  port: Int = Properties.envOrNone("PORT").map(_.toInt).getOrElse(9981),
  resourcesRoute: Route = Page.createRouteOpt()
)

object Main {


  def main(args: Array[String]): Unit = {
    run(
      MainParameters()
    )
  }

  def run(
    params: MainParameters
  ): Unit = {
    implicit val actorSystem = ActorSystem()
    implicit val materializer = ActorMaterializer()
    import actorSystem.dispatcher

    import Directives._


    import Page._
    val pageHtmlString = htmlString(
      Seq(
        DepsJs,
        MainJS,
        LaunchJS
      )
    )

    val wsFlow =
      PicklingFlow.pickling(
        LogicBasicFlow.createLogic()
      )

    val route =
      pathEnd {
        redirect(Uri./, StatusCodes.MovedPermanently)
      } ~
      pathSingleSlash {
        complete(
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            pageHtmlString
          )
        )
      } ~
      path(Shared.WebsocketPathElement) {
        handleWebSocketMessages(
          wsFlow
        )
      } ~
      params.resourcesRoute

    Http()
      .bindAndHandle(
        route,
        "0.0.0.0",
        params.port
      )
      .onComplete({ result =>
        actorSystem.log.info(result.toString)
      })
  }

}
