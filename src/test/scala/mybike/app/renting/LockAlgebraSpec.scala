package mybike.app.renting

import cats.effect.IO

import scala.concurrent.Future
import org.scalatest._

import mybike.domain.Lock
import mybike.app.fixtures.LockFixture

class LockAlgebraSpec extends AsyncFunSuite with Matchers with LockFixture {

  test("LockStore - add locks") {
    IOAssertion {
      for {
        lockStore <- LocksStoreAlg.createMemStoreInterpreter[IO](List.empty[Lock])
        _         <- lockStore.save(anOpenLock())
        _         <- lockStore.save(anOpenLock())
        allLocks  <- lockStore.findAll
        rs <- IO {
          allLocks should have size 2
        }
      } yield rs
    }
  }

  test("LockStore - should disable a lock") {
    IOAssertion {
      val lock = anOpenLock()
      for {
        lockStore <- LocksStoreAlg.createMemStoreInterpreter[IO](List.empty[Lock])
        _         <- lockStore.save(lock)
        _         <- lockStore.disable(lock.id)
        foundLock <- lockStore.find(lock.id).map(_.get)
        rs <- IO {
          foundLock.isOpen shouldBe false
        }
      } yield rs
    }
  }
}

object IOAssertion {
  def apply[A](ioa: IO[Assertion]): Future[Assertion] = ioa.unsafeToFuture
}
