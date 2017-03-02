package maprohu.heroku.frontend.pages

/**
  * Created by pappmar on 02/03/2017.
  */
sealed trait MainPage

case object GuestPage extends MainPage
case object HomePage extends MainPage


