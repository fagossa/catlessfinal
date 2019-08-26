package mybike.app.engine

import java.time.Duration

import cats.effect.Sync
import mybike.domain.Point

trait PlannerChannel {
  def listen(listener: PlannerListener): Unit
}

trait PlannerAlg[F[_]] {
  def execute(request: PlannerRequest): F[PlannerChannel]
}

class PlannerInterpreter[F[_]: Sync] extends PlannerAlg[F] {
  override def execute(request: PlannerRequest): F[PlannerChannel] =
    Sync[F].delay { (listener: PlannerListener) =>
      ()
    }
}

case class PlannerResponse(
  startPos: Point,
  endPos: Point,
  pickingPoint: Point,
  returnPoint: Point,
  path: Path,
  duration: Duration
)

trait Path

case class PlannerRequest(
  startPos: Point,
  endPos: Point
)
