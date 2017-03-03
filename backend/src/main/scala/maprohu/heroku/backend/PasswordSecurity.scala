package maprohu.heroku.backend

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
  * Created by maprohu on 03-03-2017.
  */
object PasswordSecurity {

  val random = new SecureRandom()
  val secretFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
  val encoder = Base64.getEncoder

  case class Encoded(
    salt: Array[Byte],
    hash: Array[Byte],
    iterations: Int
  )

  def encode(pw: String, salt: Array[Byte], iterations: Int) : Array[Byte] = {
    val spec = new PBEKeySpec(pw.toCharArray, salt, iterations, 256)
    secretFactory.generateSecret(spec).getEncoded
  }

  def encode(pw: String) : Encoded = {
    val iterations = 65526

    val salt = Array.ofDim[Byte](32)
    random.nextBytes(salt)

    val hash = encode(pw, salt, iterations)

    Encoded(
      hash = hash,
      salt = salt,
      iterations = iterations
    )
  }

  def authenticate(pw: String, encoded: Encoded) : Boolean = {
    val hash = encode(pw, encoded.salt, encoded.iterations)

    hash sameElements encoded.hash
  }

}
