package maprohu.heroku.backend

import maprohu.heroku.backend.data.Data
import slick.jdbc.{DatabaseUrlDataSource, H2Profile}

/**
  * Created by pappmar on 03/03/2017.
  */
class H2Data extends Data with H2Profile {

  import api._

  override val db =
    Database.forURL(
      url = "jdbc:h2:file:./target/h2/data",
      driver = "org.h2.Driver"
    )

}
