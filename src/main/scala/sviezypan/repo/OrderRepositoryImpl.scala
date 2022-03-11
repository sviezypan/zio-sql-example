package sviezypan.repo

import zio._
import zio.stream._
import sviezypan.domain.AppError.RepositoryError
import sviezypan.domain._

import java.util.UUID
import java.time.LocalDate
import zio.sql.ConnectionPool

final class OrderRepositoryImpl(
    connectionPool: ConnectionPool
) extends OrderRepository
    with PostgresTableDescription {

  lazy val driverLayer = ZLayer
    .make[SqlDriver](
      SqlDriver.live,
      ZLayer.succeed(connectionPool)
    )

  override def findById(id: UUID): IO[RepositoryError, Order] = {
    val query = select(orderId ++ fkCustomerId ++ orderDate)
      .from(orders)
      .where(orderId === id)

    ZIO.logInfo(s"Query to execute findOrderById is ${renderRead(query)}") *>
      execute(query.to((Order.apply _).tupled))
        .findFirst(driverLayer, id)
  }

  override def findAll(): ZStream[Any, RepositoryError, Order] = {
    val query = select(orderId ++ fkCustomerId ++ orderDate)
      .from(orders)

    execute(query.to((Order.apply _).tupled))
      .provideDriver(driverLayer)
  }

  override def add(order: Order): IO[RepositoryError, Int] =
    insertOrder(Seq((order.id, order.customerId, order.date)))

  override def add(orders: List[Order]): IO[RepositoryError, Int] =
    insertOrder(orders.map(o => (o.id, o.customerId, o.date)))

  override def countAll(): IO[RepositoryError, Int] = {
    import AggregationDef._

    val query = select(Count(orderId)).from(orders)

    ZIO.logInfo(s"For count all, the orders query is ${renderRead(query)}") *>
      execute(query)
        .provideDriver(driverLayer)
        .runCollect
        .map(_.map(_.toInt).head)
  }

  override def removeAll(): ZIO[Any, RepositoryError, Int] =
    execute(deleteFrom(orders))
      .provideAndLog(driverLayer)

  private def insertOrder(
      data: Seq[(UUID, UUID, LocalDate)]
  ): IO[RepositoryError, Int] = {
    val query = insertInto(orders)(orderId ++ fkCustomerId ++ orderDate)
      .values(data)

    ZIO.logInfo(s"Insert order query is ${renderInsert(query)}") *>
      execute(query)
        .provideAndLog(driverLayer)
  }
}

object OrderRepositoryImpl {
  val live: ZLayer[ConnectionPool, Nothing, OrderRepository] =
    (new OrderRepositoryImpl(_)).toLayer
}
