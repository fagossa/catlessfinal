package bike

import scala.util.Try

import bike.lock.{ BikeRenting, LockPostgreRepository }

object Hello extends Greeting with App {

  // new BikeRenting[Try](new LockPostgreRepository[Try](???))

}

trait Greeting {
  lazy val greeting: String = "hello"
}
