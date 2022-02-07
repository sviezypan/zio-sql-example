package sviezypan.repo

import zio._
import zio.stream._
import zio.sql.ConnectionPool
import sviezypan.domain.{
  Customer,
  CustomerWithOrderDate,
  CustomerWithOrderNumber
}
import sviezypan.domain.DomainError.RepositoryError

import java.util.UUID
import zio.sql.ConnectionPoolConfig

final class CustomerRepositoryLive(
    poolConfig: ConnectionPoolConfig
) extends CustomerRepository
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

  def findAll(): ZStream[Any, RepositoryError, Customer] = {
    val selectAll =
      select(customerId ++ fName ++ lName ++ verified ++ dob).from(customers)

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute findAll is ${renderRead(selectAll)}")
    ) *>
      execute(selectAll.to((Customer.apply _).tupled))
        .provideDriver(driverLayer)
  }

  def findById(id: UUID): ZIO[Any, RepositoryError, Customer] = {
    val selectAll = select(customerId ++ fName ++ lName ++ verified ++ dob)
      .from(customers)
      .where(customerId === id)

    ZIO.logInfo(s"Query to execute findById is ${renderRead(selectAll)}") *>
      execute(selectAll.to((Customer.apply _).tupled))
        .findFirst(driverLayer, id)
  }

  def create(customer: Customer): ZIO[Any, RepositoryError, Unit] = {
    val query =
      insertInto(customers)(customerId ++ dob ++ fName ++ lName ++ verified)
        .values(
          (
            customer.id,
            customer.dateOfBirth,
            customer.fname,
            customer.lname,
            customer.verified
          )
        )

    ZIO.logInfo(s"Query to insert customer is ${renderInsert(query)}") *>
      execute(query)
        .provideAndLog(driverLayer)
        .unit
  }

  def create(customer: List[Customer]): ZIO[Any, RepositoryError, Int] = {
    val data =
      customer.map(c => (c.id, c.dateOfBirth, c.fname, c.lname, c.verified))

    val query =
      insertInto(customers)(customerId ++ dob ++ fName ++ lName ++ verified)
        .values(data)

    ZIO.logInfo(s"Query to insert customers is ${renderInsert(query)}") *>
      execute(query)
        .provideAndLog(driverLayer)
  }

  /** Lateral join
    *
    * select customers.first_name, customers.last_name, derived.order_date from
    * customers, lateral ( select orders.order_date from orders where
    * customers.id = orders.customer_id order by orders.order_date desc limit 1
    * ) derived order by derived.order_date desc
    */
  def findAllWithLatestOrder()
      : ZStream[Any, RepositoryError, CustomerWithOrderDate] = {
    import PostgresSpecific.PostgresSpecificTable._

    val query =
      select(fName ++ lName ++ orderDateDerived)
        .from(customers.lateral(orderDateDerivedTable))
        .orderBy(Ordering.Desc(orderDateDerived))

    ZStream.fromZIO(
      ZIO.logInfo(
        s"Query to execute findAllWithLatestOrder is ${renderRead(query)}"
      )
    ) *>
      execute(
        query
          .to((CustomerWithOrderDate.apply _).tupled)
      )
        .provideDriver(driverLayer)
  }

  /** Correlated subqueries in selection
    *
    * select first_name, last_name, ( select count(orders.id) from orders where
    * customers.id = orders.customer_id ) as "count" from customers
    */
  def findAllWithCountOfOrders()
      : ZStream[Any, RepositoryError, CustomerWithOrderNumber] = {
    import AggregationDef._
    val subquery =
      customers
        .subselect(Count(orderId))
        .from(orders)
        .where(fkCustomerId === customerId)

    val query = select(fName ++ lName ++ (subquery as "Count")).from(customers)

    ZStream.fromZIO(
      ZIO.logInfo(s"Query to execute is ${renderRead(query)}")
    ) *>
      execute(
        query
          .to((CustomerWithOrderNumber.apply _).tupled)
      )
        .provideDriver(driverLayer)
  }

  def removeAll(): ZIO[Any, RepositoryError, Int] =
    execute(deleteFrom(customers))
      .provideLayer(driverLayer)
      .mapError(e => RepositoryError(e.getCause()))
}

object CustomerRepositoryLive {
  val layer: ZLayer[ConnectionPoolConfig, Throwable, CustomerRepository] =
    (new CustomerRepositoryLive(_)).toLayer
}
