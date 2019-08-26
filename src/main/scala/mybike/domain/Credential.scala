package mybike.domain

case class Credential(
  login: String,
  password: String
)

object Credential {

  def hashPassword(password: String): String = {
    import java.security.MessageDigest
    val byteArray = MessageDigest.getInstance("MD5").digest(password.getBytes)
    new sun.misc.BASE64Encoder().encode(byteArray)
  }

  def build(login: String, password: String): Credential = {
    val hashedPassword = hashPassword(password)
    Credential(login, hashedPassword)
  }

}
