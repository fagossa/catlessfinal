package mybike.app.renting

import cats.effect.Sync
import mybike.domain.{Lock, LockId}

trait BikeRentingAlg[F[_]] {
  def rentBike(lockId: LockId): F[Either[String, Unit]]
  def releaseBike(lockId: LockId): F[Either[String, Unit]]
}

class BikeRentingInterpreter[F[_]: Sync](
  lockRepo: LocksAlg[F],
  lockCertificateRepo: LockCertificateRepositoryAlg[F]
) extends BikeRentingAlg[F] {

  override def rentBike(lockId: LockId): F[Either[String, Unit]] = {
    import cats.implicits._
    lockRepo.find(lockId).flatMap {
      case Some(lock: Lock) =>
        val updatedLock: Lock = lock.copy(open = true)
        for {
          _ <- lockRepo.save(updatedLock)
          r <- Sync[F].pure(Right(()))
        } yield (r)
      case None =>
        Sync[F].pure(Left("Lock not found"))
    }
  }

  override def releaseBike(lockId: LockId): F[Either[String, Unit]] = {
    import cats.implicits._
    lockRepo.find(lockId).flatMap {
      case Some(lock: Lock) =>
        if (lock.open) {
          val updatedLock: Lock = lock.copy(open = false)
          for {
            _ <- lockRepo.save(updatedLock)
            r <- Sync[F].pure(Right(()))
          } yield (r)
        } else {
          Sync[F].pure(Left("Lock is not closed"))
        }
      case None =>
        Sync[F].pure(Left("Lock could not be found"))
    }
  }

}
