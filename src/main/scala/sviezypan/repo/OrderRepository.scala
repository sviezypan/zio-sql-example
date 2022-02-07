package sviezypan.repo

import sviezypan.domain.DomainError.RepositoryError
import sviezypan.domain.{CustomerWithOrderDate, Order}
import zio._
import zio.stream._

trait OrderRepository {

  def findAll(): ZStream[Any, RepositoryError, Order]

  def findAllWithNames(): ZStream[Any, RepositoryError, CustomerWithOrderDate]

  def add(order: Order): IO[RepositoryError, Int]

  def addAll(orders: List[Order]): IO[RepositoryError, Int]

  def findOrderById(id: java.util.UUID): IO[RepositoryError, Order]

  def countAllOrders(): IO[RepositoryError, Int]

  def removeAll(): ZIO[Any, RepositoryError, Int]
}

object OrderRepository {
  def findAll(): ZStream[OrderRepository, RepositoryError, Order] =
    ZStream.serviceWithStream[OrderRepository](_.findAll())

  def findAllWithNames()
      : ZStream[OrderRepository, RepositoryError, CustomerWithOrderDate] =
    ZStream.serviceWithStream[OrderRepository](_.findAllWithNames())

  def add(order: Order): ZIO[OrderRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[OrderRepository](_.add(order))

  def addAll(orders: List[Order]): ZIO[OrderRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[OrderRepository](_.addAll(orders))

  def findOrderById(
      id: java.util.UUID
  ): ZIO[OrderRepository, RepositoryError, Order] =
    ZIO.serviceWithZIO[OrderRepository](_.findOrderById(id))

  def countAllOrders(): ZIO[OrderRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[OrderRepository](_.countAllOrders())

  def removeAll(): ZIO[OrderRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[OrderRepository](_.removeAll())

}
