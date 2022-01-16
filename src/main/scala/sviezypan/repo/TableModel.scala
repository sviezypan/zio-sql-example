package sviezypan.repo

import zio.sql.postgresql.PostgresModule
import zio.stream._
import zio.logging._
import zio._
import sviezypan.domain.DomainError.RepositoryError

trait TableModel extends PostgresModule {

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

  val orderDateDerivedTable = subselect[customers.TableType](orderDate)
    .from(orders)
    .limit(1)
    .where(customerId === fkCustomerId)
    .orderBy(Ordering.Desc(orderDate))
    .asTable("derived")

  val orderDateDerived :*: _ = orderDateDerivedTable.columns


  implicit class ZStreamSqlExt[T](zstream: ZStream[Has[SqlDriver], Exception, T]) {
      def provideDriver(driver: SqlDriverLive, log: Logger[String]): ZStream[Any, RepositoryError, T] =
        zstream
          .tapError(e => log.error(e.getMessage()))
          .mapError(e => RepositoryError(e.getCause()))
          .provide(Has(driver))

      def findFirst(driver: SqlDriverLive, log: Logger[String], id: java.util.UUID): ZIO[Any, RepositoryError, T] = 
        zstream
          .runHead
          .some
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

  implicit class ZioSqlExt[T](zio: ZIO[Has[SqlDriver], Exception, T]) {
    def provideAndLog(driver: SqlDriverLive, log: Logger[String]): ZIO[Any, RepositoryError, T] =
      zio
        .tapError(e => log.error(e.getMessage()))
        .mapError(e => RepositoryError(e.getCause()))
        .provide(Has(driver))
  }
}