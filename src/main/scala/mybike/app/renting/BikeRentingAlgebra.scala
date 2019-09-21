package mybike.app.renting

import cats.effect.Sync
import mybike.ErrorOr
import mybike.domain.{Lock, LockId}

trait BikeRentingAlgebra[F[_]] {
  def rent(lockId: LockId): F[ErrorOr[LockId]]
  def release(lockId: LockId): F[ErrorOr[Unit]]
}

object BikeRentingAlgebra {

  def create[F[_]: Sync](
    lockStore: LocksStoreAlg[F],
    gpsPointStore: GpsPointStoreAlg[F]
  ): BikeRentingAlgebra[F] = new BikeRentingAlgebra[F] {

    override def rent(lockId: LockId): F[ErrorOr[LockId]] = {
      import cats.implicits._
      lockStore.find(lockId).flatMap {
        case Some(lock: Lock) =>
          for {
            _ <- lockStore.save(lock.opened())
            r <- Sync[F].pure(Right(lockId))
          } yield r
        case None =>
          Sync[F].pure(Left("Lock not found"))
      }
    }

    override def release(lockId: LockId): F[ErrorOr[Unit]] = {
      import cats.implicits._
      lockStore.find(lockId).flatMap {
        case Some(lock: Lock) if lock.isOpen =>
          for {
            _ <- lockStore.save(lock.closed())
            r <- Sync[F].pure(Right(()))
          } yield r
        case Some(_) => Sync[F].pure(Left("Lock is not closed"))
        case None => Sync[F].pure(Left("Lock could not be found"))
      }
    }

  }
}
