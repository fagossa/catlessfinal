package mybike.app.engine

trait PlannerListener {
  def onResponse(response: PlannerResponse): Unit
  def onFailure(error: Throwable): Unit
}

object PlannerListener {
  def apply(f: Either[Throwable, PlannerResponse] => Unit): PlannerListener = new PlannerListener {
    override def onResponse(response: PlannerResponse): Unit = {
      println("PlannerListener::onResponse")
      f(Right(response))
    }

    override def onFailure(error: Throwable): Unit = {
      println("PlannerListener::onFailure")
      f(Left(error))
    }
  }
}
