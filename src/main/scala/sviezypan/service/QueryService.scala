package sviezypan.service

import zio.stream._
import sviezypan.domain._
import sviezypan.domain.AppError._

trait QueryService {

  def findAllWithLatestOrder()
      : ZStream[Any, RepositoryError, CustomerWithOrderDate]

  def findAllWithCountOfOrders()
      : ZStream[Any, RepositoryError, CustomerWithOrderNumber]

  def findAllWithNames(): ZStream[Any, RepositoryError, CustomerWithOrderDate]
}

object QueryService {

  def findAllWithLatestOrder()
      : ZStream[QueryService, RepositoryError, CustomerWithOrderDate] =
    ZStream.serviceWithStream[QueryService](_.findAllWithLatestOrder())

  def findAllWithCountOfOrders()
      : ZStream[QueryService, RepositoryError, CustomerWithOrderNumber] =
    ZStream.serviceWithStream[QueryService](_.findAllWithCountOfOrders())

  def findAllWithNames()
      : ZStream[QueryService, RepositoryError, CustomerWithOrderDate] =
    ZStream.serviceWithStream[QueryService](_.findAllWithNames())
}