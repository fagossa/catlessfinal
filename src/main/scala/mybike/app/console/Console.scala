package mybike.app.console

import cats.effect.Sync

import scala.util.Try

trait Console[F[_]] {
  def readLn: F[String]
  def readInt: F[Option[Int]]
  def putStrLn(str: String): F[Unit]
}

class ConsoleImpl[F[_]: Sync] extends Console[F] {
  import scala.io.StdIn.readLine
  override def readLn: F[String] = Sync[F].delay { readLine() }

  import cats.implicits._
  override def readInt: F[Option[Int]] = readLn.map { content =>
    Try(content.toInt).toOption
  }

  override def putStrLn(str: String): F[Unit] = Sync[F].delay { println(str) }
}
