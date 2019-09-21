package mybike

import java.util.UUID

import cats.effect.concurrent.Ref
import cats.effect._
import mybike.app.console.{ConsoleAlg, Menu}
import mybike.app.engine.PlannerAlg
import mybike.app.renting.{BikeRentingAlg, GpsPointStoreAlg, LocksStoreAlg, RiderAlg}
import mybike.domain.{Lock, LockId}

object Main extends IOApp {
  // IOApp already provides implicitly:
  // * ContextShift, a thread pool
  // * Timer, to be able to sleep

  override def run(args: List[String]): IO[ExitCode] = {
    val initialLocks = List(
      Lock(id = LockId(UUID.fromString("e91c80eb-7fc7-42dd-b7d8-6c06e77976f9")), isOpen = true),
      Lock(id = LockId(UUID.fromString("23ab9874-d6d3-4ca4-ad9e-dc54457ad731")), isOpen = false)
    )
    for {
      response <- program(initialLocks: _*)
      exitCode <- response.fold(_ => IO(ExitCode.Error), _ => IO(ExitCode.Success))
    } yield exitCode
  }

  def program(locks: Lock*): IO[ErrorOr[Unit]] =
    for {
      gpsStore <- IO.pure(GpsPointStoreAlg.createFileStoreInterpreter[IO])
      lockStore <- for {
        state <- Ref.of[IO, Map[LockId, Lock]](locks.map(lock => (lock.id, lock)).toMap)
        resp  <- IO.pure(LocksStoreAlg.createMemStoreInterpreter[IO](state))
      } yield resp
      renting  <- IO.pure(BikeRentingAlg.create[IO](lockStore, gpsStore))
      planner  <- IO.pure(PlannerAlg.createMemInterpreter[IO])
      rider    <- IO.pure(new RiderAlg(renting, planner, lockStore))
      console  <- IO.pure(ConsoleAlg.create[IO]())
      cli      <- IO.pure(new Menu[IO](gpsStore, lockStore, rider, console))
      response <- cli.mainMenu
    } yield response

}
