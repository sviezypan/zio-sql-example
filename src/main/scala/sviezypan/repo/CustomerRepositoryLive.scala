package sviezypan.repo

import zio._
import zio.logging._
import zio.sql.postgresql.PostgresModule
import zio.sql.ConnectionPool
import zio.blocking._
import zio.stream._
import sviezypan.domain.DomainError._
import io.regec.domain._
import sviezypan.domain.{Customer, CustomerWithOrderDate, CustomerWithOrderNumber}
import sviezypan.domain.DomainError.RepositoryError

import java.util.UUID
import java.time.LocalDate

final class CustomerRepositoryLive(
    log: Logger[String],
    blocking: Blocking.Service,
    pool: ConnectionPool,
  ) extends CustomerRepository
    with PostgresModule {

  lazy val driver = new SqlDriverLive(blocking, pool)

  import ColumnSet._

  val customers =
    (uuid("id") ++ string("first_name") ++ string("last_name") ++ boolean(
      "verified"
    ) ++ localDate("dob"))
      .table("customers")

  val customerId :*: fName :*: lName :*: verified :*: dob :*: _ =
    customers.columns

  val orders = (uuid("id") ++ uuid("customer_id") ++ localDate("order_date")).table("orders")

  val orderId :*: fkCustomerId :*: orderDate :*: _ = orders.columns

  def findAll(): ZStream[Any, RepositoryError, Customer] = {
    val selectAll = select(customerId ++ fName ++ lName ++ verified ++ dob).from(customers)

    execute(selectAll.to[UUID, String, String, Boolean, LocalDate, Customer](Customer.apply))
      .mapError(e => RepositoryError(e.getCause()))
      .provide(Has(driver))
  }

  def findById(id: UUID): ZIO[Any, RepositoryError, Customer] = {
    val selectAll = select(customerId ++ fName ++ lName ++ verified ++ dob)
      .from(customers)
      .where(customerId === id)

    log.info(s"Query to execute is ${renderRead(selectAll)}") *>
    execute(selectAll.to[UUID, String, String, Boolean, LocalDate, Customer](Customer.apply))
      .runHead
      .some
      .mapError {
        case None    => RepositoryError(new RuntimeException(s"Order with id $id does not exists"))
        case Some(e) => RepositoryError(e.getCause())
      }
      .provide(Has(driver))
  }

  def create(customer: Customer): ZIO[Any, RepositoryError, Unit] = {
    val query = insertInto(customers)(customerId ++ dob ++ fName ++ lName ++ verified)
      .values(
        (customer.id, customer.dateOfBirth, customer.fname, customer.lname, customer.verified)
      )

    execute(query)
      .tapError(e => log.error(e.getMessage()))
      .mapError(e => RepositoryError(e.getCause()))
      .provide(Has(driver))
      .unit
  }

  def create(customer: List[Customer]): ZIO[Any, RepositoryError, Int] = {
    val data = customer.map(c => (c.id, c.dateOfBirth, c.fname, c.lname, c.verified))

    val query = insertInto(customers)(customerId ++ dob ++ fName ++ lName ++ verified)
      .values(data)

    execute(query)
      .tapError(e => log.error(e.getMessage()))
      .mapError(e => RepositoryError(e.getCause()))
      .provide(Has(driver))
  }

  val orderDateDerivedTable = customers
    .subselect(orderDate)
    .from(orders)
    .limit(1)
    .where(customerId === fkCustomerId)
    .orderBy(Ordering.Desc(orderDate))
    .asTable("derived")

  val orderDateDerived :*: _ = orderDateDerivedTable.columns

  /** Lateral join
    *
    * select customers.first_name, customers.last_name, derived.order_date from customers, lateral (
    * select orders.order_date from orders where customers.id = orders.customer_id order by
    * orders.order_date desc limit 1 ) derived order by derived.order_date desc
    */
  def findAllWithLatestOrder(): ZStream[Any, RepositoryError, CustomerWithOrderDate] = {
    import PostgresSpecific.PostgresSpecificTable._

    val query =
      select(fName ++ lName ++ orderDateDerived)
        .from(customers.lateral(orderDateDerivedTable))
        .orderBy(Ordering.Desc(orderDateDerived))

    ZStream.fromEffect(log.info(s"Query to execute is ${renderRead(query)}")) *>
    execute(
      query
        .to[String, String, LocalDate, CustomerWithOrderDate](CustomerWithOrderDate.apply)
    )
      .tapError(e => log.error(e.getMessage()))
      .mapError(e => RepositoryError(e.getCause()))
      .provide(Has(driver))
  }

  /** Correlated subqueries in selection
    *
    * select first_name, last_name, ( select count(orders.id) from orders where customers.id =
    * orders.customer_id ) as "count" from customers
    */
  def findAllWithCountOfOrders(): ZStream[Any, RepositoryError, CustomerWithOrderNumber] = {
    import AggregationDef._
    val subquery =
      customers.subselect(Count(orderId)).from(orders).where(fkCustomerId === customerId)

    val query = select(fName ++ lName ++ (subquery as "Count")).from(customers)

    ZStream.fromEffect(log.info(s"Query to execute is ${renderRead(query)}")) *>
    execute(
      query
        .to[String, String, Long, CustomerWithOrderNumber](CustomerWithOrderNumber.apply)
    )
      .tapError(e => log.error(e.getMessage()))
      .mapError(e => RepositoryError(e.getCause()))
      .provide(Has(driver))
  }

  def removeAll(): ZIO[Any, RepositoryError, Int] =
    execute(deleteFrom(customers))
      .mapError(e => RepositoryError(e.getCause()))
      .provide(Has(driver))
}

object CustomerRepositoryLive {

  val layer
      : ZLayer[Has[Logger[String]] with Has[Blocking.Service] with Has[ConnectionPool], Nothing, Has[CustomerRepository]] =
    (for {
      logging <- ZIO.service[Logger[String]]
      blocking <- ZIO.service[Blocking.Service]
      connectionPool <- ZIO.service[ConnectionPool]
    } yield new CustomerRepositoryLive(logging, blocking, connectionPool)).toLayer
}
