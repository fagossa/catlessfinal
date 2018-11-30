package lock

import java.util.UUID

import cats.Id
import scala.collection.mutable.{Map => MutableMap}

object LockMemRepository extends LockRepositoryAlg[Id] {
  private val mem = MutableMap.empty[UUID, Lock]

  override def findAll: List[Lock] = mem.headOption.map(_._2).toList

  override def find(id: LockId): Id[Option[Lock]] = mem.get(id.value)

  override def save(lock: Lock): Id[Unit] = mem.update(lock.id.value, lock)

  override def disable(id: LockId): Id[Unit] = {
    mem.remove(id.value)
    ()
  }
}