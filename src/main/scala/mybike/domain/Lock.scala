package mybike.domain

import java.util.UUID

case class LockId(value: UUID) extends AnyVal

case class Lock(id: LockId, open: Boolean, hash: Option[String])
