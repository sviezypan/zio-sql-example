package sviezypan.repo

import sviezypan.domain.DomainError._
import sviezypan.domain.OrderDetail
import zio.stream._
import zio._

trait PriceRepository {
  def findOrdersWithHigherThanAvgPrice(): ZStream[Any, RepositoryError, OrderDetail]
}

object PriceRepository {
    def findOrdersWithHigherThanAvgPrice(): ZStream[Has[PriceRepository], RepositoryError, OrderDetail] = 
        ZStream.serviceWithStream[PriceRepository](_.findOrdersWithHigherThanAvgPrice())
}