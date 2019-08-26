package mybike.app.engine

trait PlannerListener {
  def onResponse(response: PlannerResponse): Unit
  def onFailure(error: Throwable): Unit
}

object PlannerListener {
  def apply(f: PlannerResponse => Unit)(g: Throwable => Unit): PlannerListener =
    new PlannerListener {
      def onResponse(response: PlannerResponse): Unit = f(response)
      def onFailure(error: Throwable): Unit = g(error)
    }
}
