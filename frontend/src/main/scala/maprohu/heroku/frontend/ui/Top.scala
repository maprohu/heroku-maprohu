package maprohu.heroku.frontend.ui

import rx.Var

case class Top(
  connected: Var[Boolean],
  content: Var[Content]
)

sealed trait Content

case class Login(

) extends Content




