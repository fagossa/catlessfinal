package mybike.app.renting

import cats.effect.Sync
import mybike.ErrorOr
import mybike.domain.{Lock, LockId}

trait BikeRentingAlg[F[_]] {
  def rent(lockId: LockId): F[ErrorOr[LockId]]
  def release(lockId: LockId): F[ErrorOr[Unit]]
}

class BikeRentingInterpreter[F[_]: Sync](
  lockStore: LocksStoreAlg[F],
  gpsPointStore: GpsPointStoreAlg[F]
) extends BikeRentingAlg[F] {

  override def rent(lockId: LockId): F[ErrorOr[LockId]] = {
    import cats.implicits._
    lockStore.find(lockId).flatMap {
      case Some(lock: Lock) =>
        val updatedLock: Lock = lock.copy(open = true)
        for {
          _ <- lockStore.save(updatedLock)
          r <- Sync[F].pure(Right(lockId))
        } yield r
      case None =>
        Sync[F].pure(Left("Lock not found"))
    }
  }

  override def release(lockId: LockId): F[ErrorOr[Unit]] = {
    import cats.implicits._
    lockStore.find(lockId).flatMap {
      case Some(lock: Lock) if lock.open =>
        for {
          _ <- lockStore.save(lock.copy(open = false))
          r <- Sync[F].pure(Right(()))
        } yield r
      case Some(_) => Sync[F].pure(Left("Lock is not closed"))
      case None => Sync[F].pure(Left("Lock could not be found"))
    }
  }

}
