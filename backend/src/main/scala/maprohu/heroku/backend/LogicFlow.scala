package maprohu.heroku.backend

import akka.NotUsed
import akka.stream.scaladsl.Flow
import maprohu.heroku.shared.ClientToServer

/**
  * Created by pappmar on 23/02/2017.
  */
object LogicFlow {

  def createLogic() : Flow[ClientToServer, OutputMessage, NotUsed] = {
    Flow[ClientToServer]
      .filter({ msg =>
        println(msg)
        false
      })
      .map(_.asInstanceOf[OutputMessage])
  }

}
