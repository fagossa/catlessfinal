package mybike.domain

import java.time.{Duration, Instant}
import java.util.UUID

case class Ride(
  id: UUID,
  startPos: GpsPoint,
  endPos: GpsPoint,
  duration: Duration,
  startTime: Instant,
  endTime: Instant,
  lockId: LockId
)
