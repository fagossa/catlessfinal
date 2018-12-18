package ride

import java.util.UUID
import java.time.Instant

import cats.effect.Async

import engine._
import models._

case class RideManager[F[_]](planner: Planner) {
  def bookRide(start: Point, end: Point)(implicit A: Async[F]): F[Either[String, Ride]] = {
    val request: PlannerRequest = PlannerRequest(start, end)
    val channel = planner.execute(request)
    val plannerResponse = A.async[PlannerResponse] { cb =>
      // TODO: do some check before going by
      val listener = PlannerListener(response => cb(Right(response)))(e => cb(Left(e)))
      channel.listen(listener)
    }

    // TODO: fetch a lock for the ride before going through
    A.map(plannerResponse) { response =>
      val ride = Ride(
        id = UUID.randomUUID(),
        startPos = response.startPos,
        endPos = response.endPos,
        duration = response.duration,
        startTime = Instant.now(),
        endTime = Instant.now(), // FIXME:
        lockId = "" // FIXME:
      )

      Right(ride)
    }
  }
}
