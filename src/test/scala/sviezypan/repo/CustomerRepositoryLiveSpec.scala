package sviezypan.repo

import zio.test._

import zio.test.Assertion._
import zio.test.TestAspect._
import sviezypan.domain.Customer
import java.util.UUID
import java.time.LocalDate
import zio.ZLayer
import sviezypan.repo.postgresql.PostgresContainer
import zio.sql.ConnectionPool

object CustomerRepositoryLiveSpec extends ZIOSpecDefault {

  val customerId1 = UUID.randomUUID()

  val customers = List(
    Customer(customerId1, "Peter", "Schwarz", false, LocalDate.now()),
    Customer(UUID.randomUUID(), "Laszlo", "Wider", true, LocalDate.now())
  )

  val testLayer = ZLayer.make[CustomerRepository](
    CustomerRepositoryImpl.live,
    PostgresContainer.connectionPoolConfigLayer,
    ConnectionPool.live,
    PostgresContainer.createContainer
  )

  override def spec =
    suite("customer repository test with postgres test container")(
      test("count all customers") {
        for {
          count <- CustomerRepository.findAll().runCount
        } yield assert(count)(equalTo(5L))
      },
      test("insert two new customers") {
        for {
          oneRow <- CustomerRepository.add(customers)
          count <- CustomerRepository.findAll().runCount
        } yield assert(oneRow)(equalTo(2)) && assert(count)(equalTo(7L))
      },
      test("get inserted customer") {
        for {
          customer <- CustomerRepository.findById(customerId1)
        } yield assert(customer.fname)(equalTo("Peter"))
      }
    ).provideCustomLayerShared(testLayer.orDie) @@ sequential
}
