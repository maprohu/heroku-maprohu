package maprohu.heroku.backend.data

import slick.jdbc.{DatabaseUrlDataSource, PostgresProfile}

class PGData extends Data with PostgresProfile {

  import api._

  override val db =
    Database.forDataSource(
      new DatabaseUrlDataSource,
      None
    )




}
