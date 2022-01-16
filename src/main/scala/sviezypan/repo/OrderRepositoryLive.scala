package sviezypan.repo

import zio._
import zio.stream._
import zio.logging.Logging
import sviezypan.domain.DomainError.RepositoryError
import sviezypan.domain.{CustomerWithOrderDate, Order}
import zio.sql.ConnectionPool
import zio.logging.Logger
import zio.blocking._

import java.util.UUID
import java.time.LocalDate

final class OrderRepositoryLive(
    log: Logger[String],
    blocking: Blocking.Service,
    pool: ConnectionPool,
  ) extends OrderRepository
    with TableModel {

  lazy val driver = new SqlDriverLive(blocking, pool)

  def findOrderById(id: UUID): IO[RepositoryError, Order] = {
    val query = select(orderId ++ fkCustomerId ++ orderDate)
      .from(orders)
      .where(orderId === id)

    log.info(s"Query to execute findOrderById is ${renderRead(query)}") *>
    execute(query.to[UUID, UUID, LocalDate, Order](Order.apply))
      .findFirst(driver, log, id)
  }

  def findAll(): ZStream[Any, RepositoryError, Order] = {
    val query = select(orderId ++ fkCustomerId ++ orderDate)
      .from(orders)

    execute(query.to[UUID, UUID, LocalDate, Order](Order.apply))
      .provideDriver(driver, log)
  }

  def findAllWithNames(): ZStream[Any, RepositoryError, CustomerWithOrderDate] = {
    val query = select(fName ++ lName ++ orderDate)
      .from(customers.join(orders).on(fkCustomerId === customerId))

    ZStream.fromEffect(log.info(s"Query to execute findAllWithNames is ${renderRead(query)}")) *>
    execute(
      query
        .to[String, String, LocalDate, CustomerWithOrderDate](CustomerWithOrderDate.apply)
    )
      .provideDriver(driver, log)
  }

  def add(order: Order): IO[RepositoryError, Int] =
    insertOrder(Seq((order.id, order.customerId, order.date)))

  def addAll(orders: List[Order]): IO[RepositoryError, Int] =
    insertOrder(orders.map(o => (o.id, o.customerId, o.date)))

  def countAllOrders(): IO[RepositoryError, Int] = {
    import AggregationDef._

    val query = select(Count(orderId)).from(orders)

    log.info(s"For count all, the orders query is ${renderRead(query)}") *>
    execute(query.to[Long, Int](_.toInt))
      .provideDriver(driver, log)
      .runCollect
      .map(_.head)
  }

  def removeAll(): ZIO[Any, RepositoryError, Int] =
    execute(deleteFrom(orders))
      .provideAndLog(driver, log)

  private def insertOrder(data: Seq[(UUID, UUID, LocalDate)]): IO[RepositoryError, Int] = {
    val query = insertInto(orders)(orderId ++ fkCustomerId ++ orderDate)
      .values(data)

    log.info(s"Insert order query is ${renderInsert(query)}") *>
    execute(query)
      .provideAndLog(driver, log)
  }
}

object OrderRepositoryLive {

  val layer: ZLayer[Logging with Blocking with Has[ConnectionPool], Nothing, Has[OrderRepository]] =
    (for {
      logging <- ZIO.service[Logger[String]]
      blocking <- ZIO.service[Blocking.Service]
      connectionPool <- ZIO.service[ConnectionPool]
    } yield new OrderRepositoryLive(logging, blocking, connectionPool)).toLayer
}
