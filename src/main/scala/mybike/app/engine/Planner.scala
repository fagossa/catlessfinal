package mybike.app.engine

import java.time.Duration
import java.time.temporal.ChronoUnit

import cats.effect.Sync
import mybike.domain.GpsPoint

trait PlannerChannel {
  def listen(listener: PlannerListener): Unit
}

trait PlannerAlg[F[_]] {
  def execute(request: PlannerRequest): F[PlannerChannel]
}

class MemPlannerInterpreter[F[_]: Sync] extends PlannerAlg[F] {
  override def execute(request: PlannerRequest): F[PlannerChannel] = {
    println("PlannerAlg::execute")
    Sync[F].delay {
      new PlannerChannel() { // do not transform into a lambda for clarity
        def listen(listener: PlannerListener): Unit = {
          println("PlannerAlg::listen")
          // Note: this is a hardcoded response
          val response = PlannerResponse(
            request.startPos,
            request.endPos,
            List(request.startPos, request.endPos),
            Duration.of(2, ChronoUnit.MINUTES)
          )
          listener.onResponse(response)
          ()
        }
      }
    }
  }
}

case class PlannerResponse(
  startPos: GpsPoint,
  endPos: GpsPoint,
  path: List[GpsPoint],
  duration: Duration
)

case class PlannerRequest(
  startPos: GpsPoint,
  endPos: GpsPoint
)
