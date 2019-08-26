package mybike.app.auth

import java.util.UUID

import cats.effect.Async
import mybike.domain.{Credential, Session}

class AuthService[F[_]: Async](
  credentialStore: CredentialStore[F],
  sessionStore: SessionStore[F],
  userStore: UserAlgebra[F]
) {

  def register(credential: Credential)(implicit A: Async[F]): F[Either[String, Unit]] = {
    A.ifM(credentialStore.check(credential))(
      A.map(credentialStore.saveCredential(credential))(Right.apply),
      A.pure(Left("credential already exists."))
    )
  }

  def unregister(username: String, password: String): F[Either[String, Unit]] = ???

  def login(credential: Credential)(implicit A: Async[F]): F[Either[String, UUID]] = {
    val session = Session.create(credential.login)
    A.ifM(credentialStore.check(credential))(
      A.*>(sessionStore.createSession(session))(A.pure(Right(session.id))),
      A.pure(Left("User not found"))
    )
  }

  def getSession(id: UUID)(implicit A: Async[F]): F[Either[String, Session]] = ???
  def logout(id: UUID)(implicit A: Async[F]): F[Either[String, Unit]] = ???
}

trait CredentialStore[F[_]] {
  def saveCredential(credential: Credential): F[Unit]
  def check(credential: Credential): F[Boolean]
  def deactivateCredential(login: String): F[Unit]
}

trait SessionStore[F[_]] {
  def createSession(session: Session): F[Unit]
  def findSession(id: UUID): F[Option[Session]]
  def deleteSession(id: UUID): F[Either[String, Unit]]
}
