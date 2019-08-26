package mybike

import java.util.UUID
import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import cats.effect.{ContextShift, IO, Timer}
import cats.effect.concurrent.Ref
import mybike.app.engine.PlannerAlg
import mybike.app.renting.{
  BikeRentingAlg,
  BikeRentingInterpreter,
  LockCertificateRepositoryAlg,
  LocksAlg,
  MemLocksRepository,
  RideAlgebra
}
import mybike.domain.{Lock, LockId, Point}

class Program()(
  implicit
  cs: ContextShift[IO],
  timer: Timer[IO]) {
  val ride: IO[RideAlgebra[IO]] = for {
    state <- Ref.of[IO, Map[LockId, Lock]](Map.empty[LockId, Lock])

    locks: LocksAlg[IO] = new MemLocksRepository(state)
    locksCertificate: LockCertificateRepositoryAlg[IO] = ???
    bikeRenting: BikeRentingAlg[IO] = new BikeRentingInterpreter(locks, locksCertificate)
    planner: PlannerAlg[IO] = ???

    rideProgram = new RideAlgebra(bikeRenting, planner, locks)
  } yield rideProgram
}

object Main extends App {
  private val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO] = IO.timer(ec)

  val from: Point = Point("la défense", "0.1", "0.12")
  val to: Point = Point("le louvre", "0.511", "0.523")
  val id = LockId(UUID.randomUUID())
  new Program().ride.map {
    _.bookRide(id, from, to).unsafeRunSync() match {
      case Left(error) => println(s"ERROR: $error")
      case Right(ride) => println(s"INFO: $ride")
    }
  }
}
