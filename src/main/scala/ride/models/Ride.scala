package ride.models

import java.util.UUID
import java.time.{ Duration, Instant }

case class Ride(id: UUID, startPos: Point, endPos: Point, duration: Duration, startTime: Instant, endTime: Instant, lockId: String)
