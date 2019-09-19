package mybike.app.renting

import java.io.InputStreamReader
import java.util.stream.Collectors

import cats.effect.Resource
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
    Resource.make {
      Sync[F].delay(new BufferedReader(new InputStreamReader(getClass.getResourceAsStream("/gps-points.txt"))))
    } { reader =>
      import cats.implicits._
      Sync[F].delay(reader.close()).handleErrorWith(_ => Sync[F].unit)
    }.use { br =>
      import scala.collection.JavaConverters._
      import cats.implicits._
      br.lines().collect(Collectors.toList()).asScala.toList.flatMap(GpsPoint.fromCsv).pure[F]
    }
  }

  import cats.implicits._
  override def findMostPopularTuple: F[Option[(GpsPoint, GpsPoint)]] =
    findAll.map {
      case h1 :: h2 :: _ => Some((h1, h2))
      case _ => None
    }

}
