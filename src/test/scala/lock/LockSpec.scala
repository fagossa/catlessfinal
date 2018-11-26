package lock

import example.Hello
import org.scalatest._

class LockSpec extends FlatSpec with Matchers {
  "The Hello object" should "say hello" in {
    Hello.greeting shouldEqual "hello"
  }
}
