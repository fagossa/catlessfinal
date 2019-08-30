package mybike.domain

import java.util.UUID

import scala.util.Try

case class GpsPoint(id: UUID, name: String, lat: String, long: String)

object GpsPoint {
  def fromCsv(line: String): Option[GpsPoint] = line.split(",").toList.map(_.trim) match {
    case id :: name :: lat :: long :: Nil => Try(GpsPoint(UUID.fromString(id), name, lat, long)).toOption
    case _ => None
  }
}
