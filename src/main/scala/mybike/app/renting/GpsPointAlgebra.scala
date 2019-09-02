package mybike.app.renting

import java.io.InputStreamReader

import mybike.domain.GpsPoint

trait GpsPointStoreAlg[F[_]] {
  def findAll: F[List[GpsPoint]]
  def findMostPopularTuple: F[Option[(GpsPoint, GpsPoint)]]
}

import cats.effect.Sync
class MemGpsPointStoreInterpreter[F[_]](
  implicit S: Sync[F]
) extends GpsPointStoreAlg[F] {

  override def findAll: F[List[GpsPoint]] = {
    import java.io.BufferedReader
    val acquire: F[BufferedReader] = S.delay {
      val is = getClass.getResourceAsStream("/gps-points.txt")
      new BufferedReader(new InputStreamReader(is))
    }

    S.bracket(acquire) { br =>
      import java.util.stream.Collectors
      import scala.collection.JavaConverters._
      import cats.implicits._
      br.lines().collect(Collectors.toList()).asScala.toList.flatMap(GpsPoint.fromCsv).pure[F]
    }(br => S.delay(br.close()))
  }

  import cats.implicits._
  override def findMostPopularTuple: F[Option[(GpsPoint, GpsPoint)]] =
    findAll.map {
      case h1 :: h2 :: _ => Some((h1, h2))
      case _ => None
    }

}
