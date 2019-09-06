package mybike.domain

import java.util.UUID

case class LockId(value: UUID) extends AnyVal

final case class Lock(id: LockId, isOpen: Boolean) {
  def opened(): Lock = copy(isOpen = true)

  def closed(): Lock = copy(isOpen = false)
}
