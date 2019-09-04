package mybike.app.console

import cats.Monad
import mybike.app.renting.{GpsPointStoreAlg, LocksStoreAlg, Rider}
import mybike.domain.{GpsPoint, Lock, Ride}

class Menu[F[_]: Monad](
  gpsStore: GpsPointStoreAlg[F],
  lockStore: LocksStoreAlg[F],
  rider: Rider[F],
  console: Console[F]
) {

  def mainMenu: F[Either[String, Unit]] = {
    import cats.implicits._
    import console._
    for {
      _ <- putStrLn("Welcome, please choose your option")
      _ <- putStrLn("""
                      |[1] Book a ride
                      |[2] EXIT""".stripMargin)
      n <- readInt
      resp <- n match {
        case Some(option) if option == 1 => bookRideMenu.map(_ => ().asRight[String])
        case Some(option) if option == 2 => putStrLn("Bye") *> Monad[F].pure("Terminated".asLeft[Unit])
        case _ => mainMenu
      }
    } yield resp
  }

  def bookRideMenu: F[Either[String, Unit]] = {
    import cats.implicits._
    import console._
    for {
      maybePopularGps <- gpsStore.findMostPopularTuple
      allLocks        <- lockStore.findAll
      _               <- putStrLn("Choose your lock: ")
      _ <- allLocks.mapWithIndex { (lock, index) =>
        putStrLn(s"[$index] - ${lock.id.value} - ${if (lock.open) "opened" else "closed"}")
      }.sequence
      _ <- putStrLn(s"[x] - RETURN")
      n <- readInt
      resp <- n match {
        case Some(option) if option >= 0 && option < allLocks.size =>
          bookRide(maybePopularGps, allLocks.get(option)).flatMap {
            case Right(ride) =>
              putStrLn(s">>> CREATED - Ride will take ${ride.duration.toMinutes} minutes") *> bookRideMenu
            case Left(error) =>
              putStrLn(s">>> ERROR : $error") *> bookRideMenu
          }
        case Some(_) => bookRideMenu
        case _ => mainMenu
      }
    } yield resp
  }

  private def bookRide(
    maybePopularGps: Option[(GpsPoint, GpsPoint)],
    maybeLock: Option[Lock]
  ): F[Either[String, Ride]] = {
    import cats.implicits._
    (maybeLock, maybePopularGps).mapN {
      case (lock, (pos1, pos2)) => rider.bookRide(lock.id, pos1, pos2)
    } match {
      case Some(result) =>
        result
      case None =>
        console
          .putStrLn(s">>> Impossible to create a ride")
          .map(_ => "No lock is available".asLeft[Ride])
    }
  }
}
