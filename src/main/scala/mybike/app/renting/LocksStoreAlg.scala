package mybike.app.renting

import mybike.domain.{Lock, LockId}

trait LocksStoreAlg[F[_]] {
  def findAll: F[Vector[Lock]]
  def find(id: LockId): F[Option[Lock]]
  def isOpen(id: LockId): F[Boolean]
  def save(lock: Lock): F[Unit]
  def disable(id: LockId): F[Unit]
}

object LocksStoreAlg {

  import cats.effect.Sync
  import cats.effect.concurrent.Ref

  def createMemStoreInterpreter[F[_]](ref: Ref[F, Map[LockId, Lock]])(
    implicit S: Sync[F]
  ): LocksStoreAlg[F] = new LocksStoreAlg[F] {

    import cats.implicits._

    override def findAll: F[Vector[Lock]] = ref.get.map(_.values.toVector)

    override def find(id: LockId): F[Option[Lock]] = findAll.map(_.find(_.id == id))

    override def isOpen(id: LockId): F[Boolean] = find(id).map(_.exists(_.isOpen))

    override def save(lock: Lock): F[Unit] = ref.update { content =>
      content + (lock.id -> lock)
    }

    override def disable(id: LockId): F[Unit] = find(id).flatMap {
      case Some(lock) => save(lock.closed())
      case None => S.unit
    }

  }
}
