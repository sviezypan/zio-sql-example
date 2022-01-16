package sviezypan.repo

import zio.test._
import zio.logging._
// import zio.sql.ConnectionPool
// import sviezypan.config.configuration._
// import zio.test.Assertion._
// import zio.magic._
import sviezypan.domain.Customer
import java.util.UUID
import java.time.LocalDate
import zio.test.TestAspect._


object CustomerRepositoryLiveSpec extends DefaultRunnableSpec{

  val customerId1 = UUID.randomUUID()
  val customerId2 = UUID.randomUUID()

  val customers = List(
    Customer(customerId1, "Peter", "Schwarz", false, LocalDate.now()),
    Customer(customerId2, "Laszlo", "Wider", true, LocalDate.now()),
  )

  val loggingEnv = 
    Logging.console(LogLevel.Info, LogFormat.ColoredLogFormat()) >>>
        Logging.withRootLoggerName("zio-sql-example")

  override def spec = 
    suite("customer repository test with postgres test container")(
      testM("insert customer") {
        // for {
        //   oneRow <- CustomerRepository.create(customers)
        // } yield assert(oneRow)(equalTo(2))
        ???
      },
      testM("get inserted customer") {
        ???
      },
      testM("get all customers") {
        ???
      }
    ) @@ sequential
    // .injectCustom(
    //   loggingEnv,
    //     ConnectionPool.live,
    //     DbConfig.layer.mapError(_ => new RuntimeException("")),
    //     DbConfig.connectionPoolConfig,
    //     CustomerRepositoryLive.layer,
    // )
  }