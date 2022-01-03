package sviezypan.repo

import zio._
import zio.stream._
import zio.logging.Logging
import sviezypan.domain.DomainError.RepositoryError
import sviezypan.domain.{CustomerWithOrderDate, Order}
import zio.sql.postgresql.PostgresModule
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
    with PostgresModule {

  lazy val driver = new SqlDriverLive(blocking, pool)

  // description of tables
  import ColumnSet._

  val orders = (uuid("id") ++ uuid("customer_id") ++ localDate("order_date")).table("orders")

  val orderId :*: fkCustomerId :*: orderDate :*: _ = orders.columns

  val customers =
    (uuid("id") ++ localDate("dob") ++ string("first_name") ++ string("last_name") ++ boolean(
      "verified"
    )).table("customers")

  val customerId :*: dob :*: fName :*: lName :*: verified :*: _ =
    customers.columns

  def findOrderById(id: UUID): IO[RepositoryError, Order] = {
    val query = select(orderId ++ fkCustomerId ++ orderDate)
      .from(orders)
      .where(orderId === id)

    execute(query.to[UUID, UUID, LocalDate, Order](Order.apply))
      .runHead
      .some
      //TODO create some extension method for all this boilerplate
      .tapError{
        case None    => ZIO.unit
        case Some(e) => log.error(e.getMessage())
      }
      .mapError {
        case None    => RepositoryError(new RuntimeException(s"Order with id $id does not exists"))
        case Some(e) => RepositoryError(e.getCause())
      }
      .provide(Has(driver))
  }

  def findAll(): ZStream[Any, RepositoryError, Order] = {
    val query = select(orderId ++ fkCustomerId ++ orderDate)
      .from(orders)

    execute(query.to[UUID, UUID, LocalDate, Order](Order.apply))
      .tapError(e => log.error(e.getMessage()))
      .mapError(e => RepositoryError(e.getCause()))
      .provide(Has(driver))
  }

  def findAllWithNames(): ZStream[Any, RepositoryError, CustomerWithOrderDate] = {
    val query = select(fName ++ lName ++ orderDate)
      .from(customers.join(orders).on(fkCustomerId === customerId))

    execute(
      query
        .to[String, String, LocalDate, CustomerWithOrderDate](CustomerWithOrderDate.apply)
    )
      .tapError(e => log.error(e.getMessage()))
      .mapError(e => RepositoryError(e.getCause()))
      .provide(Has(driver))
  }

  def add(order: Order): IO[RepositoryError, Int] =
    insertOrder(Seq((order.id, order.customerId, order.date)))

  def addAll(orders: List[Order]): IO[RepositoryError, Int] =
    insertOrder(orders.map(o => (o.id, o.customerId, o.date)))

  def countAllOrders(): IO[RepositoryError, Int] = {
    import AggregationDef._

    val query = select(Count(orderId)).from(orders)

    log.info(s"Count query is ${renderRead(query)}") *>
    execute(query.to[Long, Int](_.toInt))
      .tapError(e => log.error(e.getMessage()))
      .mapError(e => RepositoryError(e.getCause()))
      .provide(Has(driver))
      .runCollect
      .map(_.head)
  }

  def removeAll(): ZIO[Any, RepositoryError, Int] =
    execute(deleteFrom(orders))
      .tapError(e => log.error(e.getMessage()))
      .mapError(e => RepositoryError(e.getCause()))
      .provide(Has(driver))

  private def insertOrder(data: Seq[(UUID, UUID, LocalDate)]): IO[RepositoryError, Int] = {
    val query = insertInto(orders)(orderId ++ fkCustomerId ++ orderDate)
      .values(data)

    log.info(s"Insert query is ${renderInsert(query)}") *>
    execute(query)
      .tapError(e => log.error(e.getMessage()))
      .mapError(e => RepositoryError(e.getCause()))
      .provide(Has(driver))
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
