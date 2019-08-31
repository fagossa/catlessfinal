package mybike.app.renting

import java.time.Instant
import java.util.UUID

import cats.effect.{Concurrent, Sync}
import mybike.app.engine._
import mybike.domain.{GpsPoint, LockId, Ride}

class RideAlgebra[F[_]](
  bikeRenting: BikeRentingAlg[F],
  planner: PlannerAlg[F],
  locks: LocksStoreAlg[F]
)(
  implicit C: Concurrent[F] // Async with cancellation
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
        _ <- EitherT(bikeRenting.rentBike(lockId))
        response <- EitherT(plannerResponse.flatMap { plannerResponse =>
          buildRide(plannerResponse, lockId)
        }.map(_.asRight[String]))
      } yield response).value
    }

    C.ifM(locks.find(lockId).map(_.isDefined))(
      handleExistingLock(lockId).value,
      C.pure(Left(s"Lock with id <$lockId> does not exist"))
    )
  }

  private def buildRide(response: PlannerResponse, lockId: LockId): F[Ride] = {
    for {
      now <- Sync[F].delay { Instant.now() }
      ride <- Ride(
        id = UUID.randomUUID(),
        startPos = response.startPos,
        endPos = response.endPos,
        duration = response.duration,
        startTime = now,
        endTime = now,
        lockId = lockId
      ).pure[F]
    } yield ride
  }

}
