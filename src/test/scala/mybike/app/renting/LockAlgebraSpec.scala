package mybike.app.renting

import cats.effect.IO
import cats.effect.concurrent.Ref
import mybike.app.fixtures.LockFixture

import scala.concurrent.Future
import org.scalatest._
import mybike.domain.{Lock, LockId}

class LockAlgebraSpec extends AsyncFunSuite with Matchers with LockFixture {

  test("LockStore - add locks") {
    IOAssertion {
      for {
        ref       <- Ref.of[IO, Map[LockId, Lock]](Map.empty[LockId, Lock])
        lockStore <- IO.pure(new MemLocksStoreStore[IO](ref))
        _         <- lockStore.save(anOpenLock())
        _         <- lockStore.save(anOpenLock())
        allLocks  <- lockStore.findAll
        rs <- IO {
          allLocks should have size 2
        }
      } yield (rs)
    }
  }
}

object IOAssertion {
  def apply[A](ioa: IO[Assertion]): Future[Assertion] = ioa.unsafeToFuture
}
