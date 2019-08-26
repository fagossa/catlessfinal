package mybike.domain

import java.time.{Duration, Instant}
import java.util.UUID

case class Ride(
  id: UUID,
  startPos: Point,
  endPos: Point,
  duration: Duration,
  startTime: Instant,
  endTime: Instant,
  lockId: LockId
)
