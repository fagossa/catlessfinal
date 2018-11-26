package lock

import cats.Id
import scala.collection.mutable.{Map => MutableMap}

object LockMemRepository extends LockRepository[Id] {
  private val mem = MutableMap.empty[String, Lock]

  override def findAll: List[Lock] = mem.headOption.map(_._2).toList
  override def find(id: LockId): Id[Option[Lock]] = mem.get(id.value)
  override def save(item: Lock): Id[Unit] = mem.update(item.id.value, item)
  override def remove(id: LockId): Id[Unit] = {
    mem.remove(id.value)
    ()
  }
}