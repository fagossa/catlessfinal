package mybike.app.renting

import java.time.Instant
import java.util.UUID

import cats.effect.{Concurrent, Sync}
import mybike.app.engine._
import mybike.domain.{LockId, Point, Ride}

class RideAlgebra[F[_]](
  bikeRenting: BikeRentingAlg[F],
  planner: PlannerAlg[F],
  locks: LocksAlg[F]
)(
  implicit C: Concurrent[F] // Async with cancellation
) {
  import cats.implicits._

  def bookRide(
    lockId: LockId,
    start: Point,
    end: Point
  ): F[Either[String, Ride]] = {
    val request: PlannerRequest = PlannerRequest(start, end)
    val channel: F[PlannerChannel] = planner.execute(request)
    val plannerResponse: F[PlannerResponse] = C.async[PlannerResponse] { cb =>
      // TODO: complete implementation
      val listener = PlannerListener(response => cb(Right(response)))(e => cb(Left(e)))
      //channel.listen(listener)
    }

    import cats.data.EitherT
    def handleExistingLock(lockId: LockId): EitherT[F, String, Ride] =
      for {
        _ <- EitherT(bikeRenting.rentBike(lockId))
        ride = plannerResponse.flatMap(buildRide(_, lockId))
        response <- EitherT.liftF(ride)
      } yield response

    C.ifM(locks.find(lockId).map(_.isDefined))(
      handleExistingLock(lockId).value,
      C.pure(Left(s"Lock with id <${lockId}> does not exist"))
    )
  }

  private def buildRide(response: PlannerResponse, lockId: LockId): F[Ride] =
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
    } yield (ride)

}
