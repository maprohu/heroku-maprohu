package maprohu.heroku.backend

/**
  * Created by maprohu on 03-03-2017.
  */
object RunPasswordSecurity {

  def main(args: Array[String]): Unit = {
    val encoded = PasswordSecurity.encode("hello")

    println(PasswordSecurity.authenticate("hello", encoded))
    println(PasswordSecurity.authenticate("hello2", encoded))
  }

}
