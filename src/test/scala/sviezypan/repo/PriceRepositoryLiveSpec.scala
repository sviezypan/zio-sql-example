package sviezypan.repo

import zio._
//import zio.test.Assertion._ 
import zio.test._
import zio.logging._
import zio.blocking._
import zio.sql.ConnectionPool

object PriceRepositoryLiveSpec extends DefaultRunnableSpec {

  val priceRepositoryZIO = 
    for {
      logger <- ZIO.service[Logger[String]]
      blocker <- ZIO.service[Blocking.Service]
      pool <- ZIO.service[ConnectionPool]
    } yield new PriceRepositoryLive(logger, blocker, pool)

  def spec = 
    testM("test") {
      val orderDetails = for {
        repo <- priceRepositoryZIO
        details    <- repo.findOrdersWithHigherThanAvgPrice().runCollect
      } yield (details)

      val _ = orderDetails

      //assertM(orderDetails.map(_.size))(equalTo(1))
      ???
    }






}