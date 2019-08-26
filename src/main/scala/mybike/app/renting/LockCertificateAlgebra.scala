package mybike.app.renting

import mybike.domain.LockCertificate

trait LockCertificateRepositoryAlg[F[_]] {
  def findAll: F[List[LockCertificate]]
}

import cats.effect.Sync
import cats.effect.concurrent.Ref
class MemLockCertificateRepositoryInterpreter[F[_]](
  implicit S: Sync[F],
  ref: Ref[F, Int]
) extends LockCertificateRepositoryAlg[F] {

  override def findAll: F[List[LockCertificate]] = {
    import java.io.{BufferedReader, File, FileReader}
    val acquire: F[BufferedReader] = S.delay {
      val file = new File(getClass.getClassLoader.getResource("lock-hash.txt").getFile)
      new BufferedReader(new FileReader(file))
    }

    S.bracket(acquire) { br =>
      import java.util.stream.Collectors
      import scala.collection.JavaConverters._
      import cats.implicits._
      br.lines().collect(Collectors.toList()).asScala.toList.flatMap(LockCertificate.fromCsv).pure[F]
    }(br => S.delay(br.close()))
  }

}
