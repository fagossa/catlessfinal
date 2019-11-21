package mybike.app.console

import cats.{Applicative, Monad}
import cats.effect.Timer
import mybike.ErrorOr
import mybike.app.renting.{GpsPointStoreAlg, LocksStoreAlg, RiderProgram}
import mybike.domain.{GpsPoint, Lock, Ride}

class Menu[F[_]: Monad: Timer](
  gpsStore: GpsPointStoreAlg[F],
  lockStore: LocksStoreAlg[F],
  rider: RiderProgram[F],
  console: ConsoleAlg[F]
) {

  def mainMenu: F[ErrorOr[Unit]] = {
    import cats.implicits._
    import console._
    for {
      _ <- cleanScreen
      _ <- putStrLn("Welcome, please choose your option")
      _ <- putBoldLine("[1] Book a ride")
      _ <- putStrLn("[2] EXIT")
      n <- readInt
      resp <- n match {
        case Some(option) if option == 1 => bookRideMenu.map(_ => ().asRight[String])
        case Some(option) if option == 2 => cleanScreen *> putStrLn("Bye") *> Monad[F].pure("Terminated".asLeft[Unit])
        case _ => mainMenu
      }
    } yield resp
  }

  def bookRideMenu: F[ErrorOr[Unit]] = {
    import cats.implicits._
    import console._
    import scala.concurrent.duration._
    for {
      _               <- cleanScreen
      maybePopularGps <- gpsStore.findMostPopularTuple
      allLocks        <- lockStore.findAll
      _               <- putStrLn("Choose your lock: ")
      _ <- allLocks.mapWithIndex { (lock, index) =>
        putBoldLine(s"[$index] - ${lock.id.value} - ${if (lock.isOpen) "opened" else "closed"}")
      }.sequence
      _ <- putStrLn(s"[x] - RETURN")
      n <- readInt
      resp <- n match {
        case Some(option) if option >= 0 && option < allLocks.size =>
          val pause = implicitly[Timer[F]].sleep(3.seconds)
          bookRide(maybePopularGps, allLocks.get(option)).flatMap {
            case Right(ride) =>
              putInfoLine(s">>> CREATED - Ride will take ${ride.duration.toMinutes} minutes") *> pause *> bookRideMenu
            case Left(error) =>
              putErrorLine(s">>> ERROR : $error") *> pause *> bookRideMenu
          }
        case Some(_) => bookRideMenu
        case _ => mainMenu
      }
    } yield resp
  }

  private def bookRide(
    maybePopularGps: Option[(GpsPoint, GpsPoint)],
    maybeLock: Option[Lock]
  ): F[ErrorOr[Ride]] = {
    import cats.implicits._
    (maybeLock, maybePopularGps).mapN {
      case (lock, (pos1, pos2)) => rider.bookRide(lock.id, pos1, pos2)
    } match {
      case Some(result) => result
      case None => Applicative[F].pure("Not enough GPS positions are available to make a ride".asLeft[Ride])
    }
  }
}
