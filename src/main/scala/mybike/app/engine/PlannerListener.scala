package mybike.app.engine

trait PlannerListener {
  def onResponse(response: PlannerResponse): Unit
  def onFailure(error: Throwable): Unit
}

object PlannerListener {
  type AsyncCallback = Either[Throwable, PlannerResponse] => Unit

  def handle(f: AsyncCallback): PlannerListener = new PlannerListener {
    override def onResponse(response: PlannerResponse): Unit = {
      f(Right(response))
    }

    override def onFailure(error: Throwable): Unit = {
      f(Left(error))
    }
  }
}
