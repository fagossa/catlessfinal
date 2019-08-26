package mybike.domain

import java.util.UUID
import scala.util.Try

case class LockId(value: UUID) extends AnyVal

case class Lock(id: LockId, open: Boolean, hash: Option[String])

case class LockCertificate(id: UUID, name: String)

object LockCertificate {
  def fromCsv(line: String): Option[LockCertificate] = line.split(",").toList.map(_.trim) match {
    case id :: name :: Nil => Try(LockCertificate(UUID.fromString(id), name)).toOption
    case _ => None
  }
}
