package sviezypan.repo

import zio.test._
import zio.test.Assertion._
import zio.logging._
import zio.blocking._
import zio.clock._
import sviezypan.repo.postgresql._
import zio.sql.ConnectionPool
import zio.test.TestAspect._
import sviezypan.domain.Order
import java.util.UUID
import java.time.LocalDate

object OrderRepositoryLiveSpec extends DefaultRunnableSpec {

  val loggingEnv = 
    Logging.console(LogLevel.Info, LogFormat.ColoredLogFormat()) >>>
        Logging.withRootLoggerName("zio-sql-example")    
      
  val containerLayer = PostgresContainer.make() ++ Blocking.live ++ Clock.live    

  val connectionPoolLayer = (containerLayer >+> PostgresContainer.connectionPoolConfigLayer) >+> ConnectionPool.live

  val testLayer = (connectionPoolLayer ++ loggingEnv) >>> OrderRepositoryLive.layer

  val order = Order(UUID.randomUUID(), UUID.randomUUID(), LocalDate.now())

  def spec = 
    suite("order repository test with postgres test container")(
      testM("count all orders") {
        for {
          count <- OrderRepository.countAllOrders()
        } yield assert(count)(equalTo(25))
      },
      testM("insert new order") {
        for {
          _ <- OrderRepository.add(order)
          count <- OrderRepository.countAllOrders()
        } yield assert(count)(equalTo(26))
      }
    ).provideCustomLayerShared(testLayer.orDie) @@ sequential
}