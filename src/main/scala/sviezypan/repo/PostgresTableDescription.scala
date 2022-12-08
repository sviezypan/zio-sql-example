package sviezypan.repo

import zio.sql.postgresql.PostgresJdbcModule
import zio.stream._
import zio._
import sviezypan.domain.AppError.RepositoryError
import zio.schema.DeriveSchema
import sviezypan.domain._

trait PostgresTableDescription extends PostgresJdbcModule {

  implicit val customerSchema = DeriveSchema.gen[Customer]
  implicit val orderSchema = DeriveSchema.gen[Order]

  val customers = defineTableSmart[Customer]

  val (customerId, fName, lName, verified, dob) =
    customers.columns

  val orders = defineTableSmart[Order]

  val (orderId, fkCustomerId, orderDate) = orders.columns

  val orderDateDerivedTable = subselect[customers.TableType](orderDate)
    .from(orders)
    .limit(1)
    .where(customerId === fkCustomerId)
    .orderBy(Ordering.Desc(orderDate))
    .asTable("derived")

  val orderDateDerived = orderDateDerivedTable.columns

  implicit class ZStreamSqlExt[T](zstream: ZStream[SqlDriver, Exception, T]) {
    def provideDriver(
        driver: ULayer[SqlDriver]
    ): ZStream[Any, RepositoryError, T] =
      zstream
        .tapError(e => ZIO.logError(e.getMessage()))
        .mapError(e => RepositoryError(e.getCause()))
        .provideLayer(driver)

    def findFirst(
        driver: ULayer[SqlDriver],
        id: java.util.UUID
    ): ZIO[Any, RepositoryError, T] =
      zstream.runHead.some
        .tapError {
          case None    => ZIO.unit
          case Some(e) => ZIO.logError(e.getMessage())
        }
        .mapError {
          case None =>
            RepositoryError(
              new RuntimeException(s"Order with id $id does not exists")
            )
          case Some(e) => RepositoryError(e.getCause())
        }
        .provide(driver)
  }

  implicit class ZioSqlExt[T](zio: ZIO[SqlDriver, Exception, T]) {
    def provideAndLog(driver: ULayer[SqlDriver]): ZIO[Any, RepositoryError, T] =
      zio
        .tapError(e => ZIO.logError(e.getMessage()))
        .mapError(e => RepositoryError(e.getCause()))
        .provide(driver)
  }
}
