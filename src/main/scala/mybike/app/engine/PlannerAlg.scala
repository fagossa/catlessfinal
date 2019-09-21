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

object PlannerAlg {

  def createMemInterpreter[F[_]: Sync]: PlannerAlg[F] = new PlannerAlg[F] {
    override def execute(request: PlannerRequest): PlannerChannel = {
      buildRequestPlanner(request)
    }

    // Note: this is a SAM for PlannerChannel[F]
    private def buildRequestPlanner(request: PlannerRequest): PlannerChannel =
      (listener: PlannerListener) => { // BTW, this is SAM
        try {
          // Note: this is a hardcoded response just for the exercise ;)
          val response = PlannerResponse(
            request.startPos,
            request.endPos,
            List(request.startPos, request.endPos),
            Duration.of(2, ChronoUnit.MINUTES)
          )

          listener.onResponse(response) // NOTE: triggers success callback
        } catch {
          case NonFatal(error) => listener.onFailure(error) // NOTE: triggers failure callback
        }
        ()
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
