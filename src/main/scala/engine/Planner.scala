package engine

import java.time.Duration

import ride.models.Point

class Planner() {
  def execute(request: PlannerRequest): PlannerChannel = ???
}

trait PlannerChannel {
  def listen(listener: PlannerListener): Unit
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
