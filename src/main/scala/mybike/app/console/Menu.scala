package mybike.app.console

import cats.Monad
import mybike.app.renting.{GpsPointStoreAlg, LocksStoreAlg, Rider}

class Menu[F[_]: Monad](
  gpsStore: GpsPointStoreAlg[F],
  lockStore: LocksStoreAlg[F],
  rider: Rider[F],
  console: Console[F]
) {

  def programLoop: F[Either[String, Unit]] = {
    import cats.implicits._
    import console._
    for {
      _ <- putStrLn("Welcome, please choose your option")
      _ <- putStrLn("""
                      |[1] Book a ride
                      |[2] Exit""".stripMargin)
      n <- readInt
      resp <- n match {
        case Some(option) if option == 1 => bookRideMenu.map(_ => ().asRight[String])
        case Some(option) if option == 2 => putStrLn("Bye") *> Monad[F].pure("Terminated".asLeft[Unit])
        case _ => programLoop
      }
    } yield resp
  }

  // TODO: implement
  def bookRideMenu: F[Unit] = {
    import cats.implicits._
    for {
      allGpsPoints <- gpsStore.findAll
      /*allLocks     <- lockStore.findAll
      response     <- rider.bookRide(allLocks(0).id, allGpsPoints(0), allGpsPoints(1))*/
    } yield (())
  }

}
