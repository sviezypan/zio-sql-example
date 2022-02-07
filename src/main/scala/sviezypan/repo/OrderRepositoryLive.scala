package sviezypan.repo

import zio._
import zio.stream._
import sviezypan.domain.DomainError.RepositoryError
import sviezypan.domain.{CustomerWithOrderDate, Order}

import java.util.UUID
import java.time.LocalDate
import zio.sql.ConnectionPoolConfig
import zio.sql.ConnectionPool

final class OrderRepositoryLive(
    poolConfig: ConnectionPoolConfig
) extends OrderRepository
    with TableModel {

  lazy val poolConfigLayer = ZLayer.succeed(poolConfig)

  lazy val driverLayer = ZLayer
    .make[SqlDriver](
      SqlDriver.live,
      ConnectionPool.live,
      poolConfigLayer,
      Clock.live
    )
    .orDie

  def findOrderById(id: UUID): IO[RepositoryError, Order] = {
    val query = select(orderId ++ fkCustomerId ++ orderDate)
      .from(orders)
      .where(orderId === id)

    ZIO.logInfo(s"Query to execute findOrderById is ${renderRead(query)}") *>
      execute(query.to((Order.apply _).tupled))
        .findFirst(driverLayer, id)
  }

  def findAll(): ZStream[Any, RepositoryError, Order] = {
    val query = select(orderId ++ fkCustomerId ++ orderDate)
      .from(orders)

    execute(query.to((Order.apply _).tupled))
      .provideDriver(driverLayer)
  }

  def findAllWithNames()
      : ZStream[Any, RepositoryError, CustomerWithOrderDate] = {
    val query = select(fName ++ lName ++ orderDate)
      .from(customers.join(orders).on(fkCustomerId === customerId))

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findAllWithNames is ${renderRead(query)}")
    ) *>
      execute(
        query
          .to((CustomerWithOrderDate.apply _).tupled)
      )
        .provideDriver(driverLayer)
  }

  def add(order: Order): IO[RepositoryError, Int] =
    insertOrder(Seq((order.id, order.customerId, order.date)))

  def addAll(orders: List[Order]): IO[RepositoryError, Int] =
    insertOrder(orders.map(o => (o.id, o.customerId, o.date)))

  def countAllOrders(): IO[RepositoryError, Int] = {
    import AggregationDef._

    val query = select(Count(orderId)).from(orders)

    ZIO.logInfo(s"For count all, the orders query is ${renderRead(query)}") *>
      execute(query)
        .provideDriver(driverLayer)
        .runCollect
        .map(_.map(_.toInt).head)
  }

  def removeAll(): ZIO[Any, RepositoryError, Int] =
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

object OrderRepositoryLive {
  val layer: ZLayer[ConnectionPoolConfig, Nothing, OrderRepository] =
    (new OrderRepositoryLive(_)).toLayer
}
