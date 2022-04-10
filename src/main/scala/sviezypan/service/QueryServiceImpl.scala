package sviezypan.service

import sviezypan.repo.PostgresTableDescription

import zio._
import zio.stream._
import zio.sql.ConnectionPool
import sviezypan.domain._
import sviezypan.domain.AppError._

final class QueryServiceImpl(
    pool: ConnectionPool
) extends QueryService
    with PostgresTableDescription {

  lazy val driverLayer = ZLayer
    .make[SqlDriver](
      SqlDriver.live,
      ZLayer.succeed(pool)
    )

  /** Lateral join
    *
    * select customers.first_name, customers.last_name, derived.order_date from
    * customers, lateral ( select orders.order_date from orders where
    * customers.id = orders.customer_id order by orders.order_date desc limit 1
    * ) derived order by derived.order_date desc
    */
  override def findAllWithLatestOrder()
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
  override def findAllWithCountOfOrders()
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
}

object QueryServiceImpl {
  val live: ZLayer[ConnectionPool, Nothing, QueryService] =
    ZLayer.fromFunction(new QueryServiceImpl(_))
}
