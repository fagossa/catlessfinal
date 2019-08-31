package mybike

import java.util.UUID
import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import cats.effect.{ContextShift, ExitCode, IO, IOApp, Timer}
import cats.effect.concurrent.Ref
import mybike.app.engine.MemPlannerInterpreter
import mybike.app.renting.{BikeRentingInterpreter, MemGpsPointStoreInterpreter, MemLocksStoreStore, RideAlgebra}
import mybike.domain.{Lock, LockId, Ride}

class ProgramContext(
  implicit
  cs: ContextShift[IO],
  timer: Timer[IO]) {

  def program(locks: Lock*): IO[Either[String, Ride]] = {
    for {
      gpsStore <- IO.pure(new MemGpsPointStoreInterpreter[IO]())
      lockStore <- for {
        state <- Ref.of[IO, Map[LockId, Lock]](locks.map(lock => (lock.id, lock)).toMap)
        resp  <- IO.pure(new MemLocksStoreStore[IO](state))
      } yield resp
      renting      <- IO.pure(new BikeRentingInterpreter[IO](lockStore, gpsStore))
      planner      <- IO.pure(new MemPlannerInterpreter[IO]())
      rider        <- IO.pure(new RideAlgebra(renting, planner, lockStore))
      allGpsPoints <- gpsStore.findAll
      allLocks     <- lockStore.findAll
      response     <- rider.bookRide(allLocks(0).id, allGpsPoints(0), allGpsPoints(1))
    } yield response
  }

}

object ProgramContext {
  def apply()(
    implicit
    cs: ContextShift[IO],
    timer: Timer[IO]) = new ProgramContext()
}

object Main extends IOApp {
  private val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  override implicit val timer: Timer[IO] = IO.timer(ec)

  override def run(args: List[String]): IO[ExitCode] = {
    val runner = ProgramContext()
    val initialLocks = List(
      Lock(id = LockId(UUID.fromString("e91c80eb-7fc7-42dd-b7d8-6c06e77976f9")), open = true, hash = None)
    )

    for {
      response <- runner.program(initialLocks: _*)
      exitCode <- response.fold(error => {
        println(s"ERROR: $error")
        IO(ExitCode.Error)
      }, ride => {
        println(s"INFO: $ride")
        IO(ExitCode.Success)
      })
    } yield exitCode
  }
}
