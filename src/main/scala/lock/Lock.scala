package lock

import java.util.UUID

case class LockId(value: UUID) extends AnyVal
case class Lock(id: LockId, open: Boolean)

trait LockRepositoryAlg[F[_]] {
  def findAll: F[List[Lock]]
  def find(id: LockId): F[Option[Lock]]
  def save(lock: Lock): F[Unit]
  def disable(id: LockId): F[Unit]
}

import doobie.implicits._
import doobie.util.transactor.Transactor
import cats.effect.Sync

class LockPostgreRepository[F[_]](xa: Transactor[F])(implicit F: Sync[F]) extends LockRepositoryAlg[F] {

  override def findAll: F[List[Lock]] = /*sql"select id, open from locks"
    .query[Lock]
    .to[List]
    .transact(xa)*/
    ???

  // TODO: implement!!!
  override def find(id: LockId): F[Option[Lock]] = F.pure(None)
  override def save(lock: Lock): F[Unit] = F.unit
  override def disable(id: LockId): F[Unit] = F.unit
}

class BikeRenting[F[_]](lockRepo: LockRepositoryAlg[F])(implicit F: Sync[F]) {
  def rentBike(lockId: LockId): F[Either[String, Unit]] = {
    F.flatMap(lockRepo.find(lockId)) {
      case Some(lock: Lock) =>
        val updatedLock: Lock = lock.copy(open = true)
        F.flatMap(lockRepo.save(updatedLock)) { _ =>
          F.pure(Right(()))
        }
      case None =>
        F.pure(Left("Lock not found"))
    }
  }
}
