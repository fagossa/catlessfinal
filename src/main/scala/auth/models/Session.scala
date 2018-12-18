package auth.models

import java.time.Instant
import java.util.UUID

case class Session(
  id: UUID,
  expireAt: Instant,
  login: String
)

object Session {
  def create(login: String): Session =
    Session(
      UUID.randomUUID(),
      Instant.now, // FIXME: add constraint for exp date
      login
    )
}