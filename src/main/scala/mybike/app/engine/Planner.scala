package mybike.app.engine

import java.time.Duration

import cats.effect.Sync
import mybike.domain.GpsPoint

trait PlannerChannel {
  def listen(listener: PlannerListener): Unit
}

trait PlannerAlg[F[_]] {
  def execute(request: PlannerRequest): F[PlannerChannel]
}

class MemPlannerInterpreter[F[_]: Sync] extends PlannerAlg[F] {
  override def execute(request: PlannerRequest): F[PlannerChannel] =
    Sync[F].delay { (listener: PlannerListener) =>
      ()
    }
}

case class PlannerResponse(
  startPos: GpsPoint,
  endPos: GpsPoint,
  pickingPoint: GpsPoint,
  returnPoint: GpsPoint,
  path: Path,
  duration: Duration
)

trait Path

case class PlannerRequest(
  startPos: GpsPoint,
  endPos: GpsPoint
)
