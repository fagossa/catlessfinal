package mybike.app.console

import cats.effect.Sync

import scala.util.Try

trait ColorfulConsole[F[_]] {
  def readLn: F[String]
  def readInt: F[Option[Int]]
  def putStrLn(str: String): F[Unit]
  def cleanScreen: F[Unit]
  def putErrorLine(str: String): F[Unit]
  def putInfoLine(str: String): F[Unit]
  def putBoldLine(str: String): F[Unit]
}

object ColorfulConsole {

  def create[F[_]: Sync](): ColorfulConsole[F] = new ColorfulConsole[F] {

    import scala.io.StdIn.readLine
    override def readLn: F[String] = Sync[F].delay {
      readLine()
    }

    import cats.implicits._
    override def readInt: F[Option[Int]] = readLn.map { content =>
      Try(content.toInt).toOption
    }

    override def putStrLn(str: String): F[Unit] = Sync[F].delay {
      println(str)
    }

    override def cleanScreen: F[Unit] = putStrLn(s"\033[2J")

    override def putErrorLine(str: String): F[Unit] = putStrLn(s"\u001b[31m$str")

    override def putInfoLine(str: String): F[Unit] = putStrLn(s"\u001b[32m$str")

    override def putBoldLine(str: String): F[Unit] = putStrLn(s"\u001b[34m$str")
  }

}
