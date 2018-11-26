package lock

case class LockId(value: String) extends AnyVal
case class Lock(id: LockId, price: BigDecimal)

trait LockRepository[F[_]] {
  def findAll: F[List[Lock]]
  def find(id: LockId): F[Option[Lock]]
  def save(lock: Lock): F[Unit]
  def remove(id: LockId): F[Unit]
}

import doobie.implicits._
import doobie.util.transactor.Transactor
import cats.effect.Sync

class LockPostgreRepository[F[_]](xa: Transactor[F])
                                 (implicit F: Sync[F]) extends LockRepository[F] {

  override def findAll: F[List[Lock]] = sql"select id, price from locks"
    .query[Lock]
    .to[List]
    .transact(xa)

  override def find(id: LockId): F[Option[Lock]] = F.pure(None)
  override def save(lock: Lock): F[Unit] = F.unit
  override def remove(id: LockId): F[Unit] = F.unit
}
