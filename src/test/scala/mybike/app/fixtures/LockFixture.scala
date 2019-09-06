package mybike.app.fixtures

import java.util.UUID

import mybike.domain.{Lock, LockId}

trait LockFixture {
  def anOpenLock(): Lock = Lock(LockId(UUID.randomUUID()), isOpen = true)
  def aDisabledOpenLock(): Lock = Lock(LockId(UUID.randomUUID()), isOpen = false)
}
