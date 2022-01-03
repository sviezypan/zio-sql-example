package sviezypan.repo

import zio.sql.ConnectionPool
import zio.logging._
import zio.blocking._
import zio.sql.postgresql.PostgresModule
import sviezypan.domain.DomainError._
import sviezypan.domain.OrderDetail
import zio.stream._
import zio._

import java.util.UUID

final class PriceRepositoryLive(
    log: Logger[String],
    blocking: Blocking.Service,
    pool: ConnectionPool,
  ) extends PriceRepository
    with PostgresModule {

  import AggregationDef._
  import ColumnSet._

  lazy val driver = new SqlDriverLive(blocking, pool)

  //description of tables
  val orderDetails =
    (uuid("order_id") ++ uuid("product_id") ++ int("quantity") ++ double("unit_price"))
      .table("order_details")

  val orderDetailsId :*: productId :*: quantity :*: unitPrice :*: _ = orderDetails.columns

  val orderDetailsDerived = select(orderDetailsId ++ productId ++ quantity ++ unitPrice)
    .from(orderDetails)
    .asTable("derived")

  val derivedOrderId :*: derivedProductId :*: derivedQuantity :*: derivedUnitPrice :*: _ =
    orderDetailsDerived.columns

  /** Correlated subquery in where clause
    *
    *  select derived.order_id, derived.product_id, derived.unit_price from order_details derived
    *  where derived.unit_price  > (select avg(order_details.unit_price) from order_details where derived.product_id = order_details.product_id)
    */
  def findOrdersWithHigherThanAvgPrice(): ZStream[Any, RepositoryError, OrderDetail] = {

    // TODO ERROR: function avg(money) does not exist on postgresql => 
    // wrongly described table, Avg(BigDecimal) would not compile
    // TODO fix in zio-sql to translate to avg(order_details.unit_price::numeric) if possible

    val query = select(derivedOrderId ++ derivedProductId ++ derivedQuantity ++ derivedUnitPrice)
      .from(orderDetailsDerived)
      .where(
        derivedUnitPrice > subselect[orderDetailsDerived.TableType](Avg(unitPrice))
          .from(orderDetails)
          .where(productId === derivedProductId)
      )

    ZStream.fromEffect(log.info(s"Executing query ${renderRead(query)}")) *>
    execute(
      query
        .to[UUID, UUID, Int, Double, OrderDetail](OrderDetail.apply)
    )
      .tapError(e => log.error(e.getMessage()))
      .mapError(e => RepositoryError(e.getCause()))
      .provide(Has(driver))
  }
}

object PriceRepositoryLive {
  val layer: ZLayer[Logging with Blocking with Has[ConnectionPool], Nothing, Has[PriceRepository]] =
    (for {
      logging <- ZIO.service[Logger[String]]
      blocking <- ZIO.service[Blocking.Service]
      connectionPool <- ZIO.service[ConnectionPool]
    } yield new PriceRepositoryLive(logging, blocking, connectionPool)).toLayer
}
