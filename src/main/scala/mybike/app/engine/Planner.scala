package mybike.app.engine

import java.time.Duration
import java.time.temporal.ChronoUnit

import cats.effect.Sync
import mybike.domain.GpsPoint

import scala.util.control.NonFatal

trait PlannerChannel {
  def listen(listener: PlannerListener): Unit
}

trait PlannerAlg[F[_]] {
  def execute(request: PlannerRequest): PlannerChannel
}

class MemPlannerInterpreter[F[_]: Sync] extends PlannerAlg[F] {
  override def execute(request: PlannerRequest): PlannerChannel = {
    buildRequestPlanner(request)
  }

  // Note: this is a SAM for PlannerChannel[F]
  private def buildRequestPlanner(request: PlannerRequest): PlannerChannel =
    (listener: PlannerListener) => {
      try {
        // Note: this is a hardcoded response
        val response = PlannerResponse(
          request.startPos,
          request.endPos,
          List(request.startPos, request.endPos),
          Duration.of(2, ChronoUnit.MINUTES)
        )
        // triggers callback
        listener.onResponse(response)
      } catch {
        case NonFatal(error) =>
          // triggers callback
          listener.onFailure(error)
      }
      ()
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
