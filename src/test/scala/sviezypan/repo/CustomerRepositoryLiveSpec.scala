package sviezypan.repo

import zio.test._
import zio.logging._
import zio.clock._
import zio.sql.ConnectionPool
import zio.test.Assertion._
import sviezypan.domain.Customer
import java.util.UUID
import java.time.LocalDate
import zio.test.TestAspect._
import zio.blocking.Blocking
import sviezypan.repo.postgresql._

object CustomerRepositoryLiveSpec extends DefaultRunnableSpec {

  val customerId1 = UUID.randomUUID()

  val customers = List(
    Customer(customerId1, "Peter", "Schwarz", false, LocalDate.now()),
    Customer(UUID.randomUUID(), "Laszlo", "Wider", true, LocalDate.now()),
  )

  val loggingEnv = 
    Logging.console(LogLevel.Info, LogFormat.ColoredLogFormat()) >>>
        Logging.withRootLoggerName("zio-sql-example")    
      
  val containerLayer = PostgresContainer.make() ++ Blocking.live ++ Clock.live    

  val connectionPoolLayer = (containerLayer >+> PostgresContainer.connectionPoolConfigLayer) >+> ConnectionPool.live

  val testLayer = (connectionPoolLayer ++ loggingEnv) >>> CustomerRepositoryLive.layer

  override def spec = 
    suite("customer repository test with postgres test container")(
      testM("count all customers") {
        for {
          count <- CustomerRepository.findAll().runCount
        } yield assert(count)(equalTo(5L))
      },
      testM("insert two new customers") {
        for {
          oneRow <- CustomerRepository.create(customers)
          count <- CustomerRepository.findAll().runCount
        } yield assert(oneRow)(equalTo(2)) && assert(count)(equalTo(7L))
      },
      testM("get inserted customer") {
        for {
          customer <- CustomerRepository.findById(customerId1)
        } yield assert(customer.fname)(equalTo("Peter"))
      }
    ).provideCustomLayerShared(testLayer.orDie) @@ sequential
  }