package lock

import java.util.UUID

case class LockId(value: UUID) extends AnyVal
case class Lock(id: LockId, open: Boolean)

trait LockRepositoryAlg[F[_]] {
  def findAll: F[List[Lock]]
  def find(id: LockId): F[Option[Lock]]
  def save(lock: Lock): F[Unit]
  def disable(id: LockId): F[Unit]
}

// TODO: implement a Ref: https://github.com/typelevel/cats-effect/blob/master/site/src/main/tut/concurrency/ref.md
import cats.effect.concurrent.Ref
import cats.effect.Sync
class FileLockRepository[F[_]](implicit F: Sync[F]) extends LockRepositoryAlg[F] {

  private def fromCsv(line: String): Option[Lock] = {
    // TODO: implement
    Option(Lock(???, ???))
  }

  // TODO: extract the file content to another concept
  override def findAll: F[List[Lock]] = {
    import java.io.{BufferedReader, File, FileReader}
    val acquire: F[BufferedReader] = F.delay {
      val file = new File(getClass.getClassLoader.getResource("locks.txt").getFile)
      new BufferedReader(new FileReader(file))
    }

    F.bracket(acquire) { br =>
      import java.util.stream.Collectors
      import scala.collection.JavaConverters._
      import cats.implicits._
      br.lines.collect(Collectors.toList).asScala.toList.flatMap(fromCsv(_)).pure[F]
    }(br => F.delay(br.close()))
  }

  import cats.syntax.functor._
  override def find(id: LockId): F[Option[Lock]] = findAll.map(_.find(_.id == id))

  override def save(lock: Lock): F[Unit] = F.unit // TODO: implement?

  override def disable(id: LockId): F[Unit] = F.unit
}
