package mybike.app.renting

import java.time.Instant
import java.util.UUID

import cats.effect.{Concurrent, Sync, Timer}
import mybike.app.engine._
import mybike.domain.{GpsPoint, LockId, Ride}

class Rider[F[_]](
  bikeRenting: BikeRentingAlg[F],
  planner: PlannerAlg[F],
  locks: LocksStoreAlg[F]
)(
  implicit C: Concurrent[F], // Async with cancellation
  T: Timer[F] // Simulate sleep
) {
  import cats.implicits._

  def bookRide(
    lockId: LockId,
    start: GpsPoint,
    end: GpsPoint
  ): F[Either[String, Ride]] = {
    val request: PlannerRequest = PlannerRequest(start, end)
    val channel: PlannerChannel = planner.execute(request)

    val plannerResponse: F[PlannerResponse] = C.async[PlannerResponse] { callback =>
      val listener: PlannerListener = PlannerListener.handle(resp => callback(resp))
      channel.listen(listener)
    }

    import cats.data.EitherT
    def handleExistingLock(lockId: LockId): F[Either[String, Ride]] = {
      (for {
        response <- EitherT(plannerResponse.flatMap { plannerResponse =>
          buildRide(plannerResponse, lockId)
        }.map(_.asRight[String]))
        _ <- EitherT(bikeRenting.rentBike(lockId))
        _ <- EitherT.liftF[F, String, Unit](sendNotifications(response))
      } yield response).value
    }

    def handleLockDoesNotExist(lockId: LockId): F[Either[String, Ride]] = {
      C.pure(Left(s"Lock with id <$lockId> does not exist"))
    }

    C.ifM(locks.isOpen(lockId))(
      handleLockDoesNotExist(lockId),
      handleExistingLock(lockId)
    )
  }

  private def sendNotifications(ride: Ride): F[Unit] = {
    import scala.concurrent.duration._
    def notifService2(ride: Ride): F[Unit] =
      logString("Calling notif service 2") *> T.sleep(2.seconds) *> logString(s"process 2 DONE")

    def notifService1(ride: Ride): F[Unit] =
      logString("Calling notif service 1") *> T.sleep(1.seconds) *> logString("process 1 DONE")

    def logString(str: String): F[Unit] =
      Sync[F].delay { println(str) }

    C.race(notifService1(ride), notifService2(ride)).void
  }

  private def buildRide(response: PlannerResponse, lockId: LockId): F[Ride] = {
    for {
      now <- Sync[F].delay { Instant.now() }
      ride <- Sync[F].pure(
        Ride(
          id = UUID.randomUUID(),
          startPos = response.startPos,
          endPos = response.endPos,
          duration = response.duration,
          startTime = now,
          endTime = now,
          lockId = lockId
        ))
    } yield ride
  }

}
