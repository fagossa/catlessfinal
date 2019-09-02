package mybike

import java.util.UUID
import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import cats.effect.{ContextShift, ExitCode, IO, IOApp, Timer}
import cats.effect.concurrent.Ref
import mybike.app.console.{ConsoleImpl, Menu}
import mybike.app.engine.MemPlannerInterpreter
import mybike.app.renting.{BikeRentingInterpreter, MemGpsPointStoreInterpreter, MemLocksStoreStore, Rider}
import mybike.domain.{Lock, LockId}

class ProgramContext(
  implicit
  cs: ContextShift[IO],
  timer: Timer[IO]) {

  def program(locks: Lock*): IO[Either[String, Unit]] = {
    for {
      gpsStore <- IO.pure(new MemGpsPointStoreInterpreter[IO]())
      lockStore <- for {
        state <- Ref.of[IO, Map[LockId, Lock]](locks.map(lock => (lock.id, lock)).toMap)
        resp  <- IO.pure(new MemLocksStoreStore[IO](state))
      } yield resp
      renting  <- IO.pure(new BikeRentingInterpreter[IO](lockStore, gpsStore))
      planner  <- IO.pure(new MemPlannerInterpreter[IO]())
      rider    <- IO.pure(new Rider(renting, planner, lockStore))
      console  <- IO.pure(new ConsoleImpl[IO]())
      cli      <- IO.pure(new Menu[IO](gpsStore, lockStore, rider, console))
      response <- cli.mainMenu
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
      Lock(id = LockId(UUID.fromString("e91c80eb-7fc7-42dd-b7d8-6c06e77976f9")), open = true),
      Lock(id = LockId(UUID.fromString("23ab9874-d6d3-4ca4-ad9e-dc54457ad731")), open = false)
    )
    for {
      response <- runner.program(initialLocks: _*)
      exitCode <- response.fold(_ => IO(ExitCode.Error), _ => IO(ExitCode.Success))
    } yield exitCode
  }
}
