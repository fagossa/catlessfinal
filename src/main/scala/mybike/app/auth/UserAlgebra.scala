package mybike.app.auth

import mybike.domain.User

trait UserAlgebra[F[_]] {
  def getByUsername(username: String): F[Either[String, User]]
}
