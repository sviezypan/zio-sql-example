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
  def findAll(): ZStream[Has[OrderRepository], RepositoryError, Order] =
    ZStream.serviceWithStream[OrderRepository](_.findAll())

  def findAllWithNames(): ZStream[Has[OrderRepository], RepositoryError, CustomerWithOrderDate] =
    ZStream.serviceWithStream[OrderRepository](_.findAllWithNames())

  def add(order: Order): ZIO[Has[OrderRepository], RepositoryError, Int] =
    ZIO.serviceWith[OrderRepository](_.add(order))

  def addAll(orders: List[Order]): ZIO[Has[OrderRepository], RepositoryError, Int] =
    ZIO.serviceWith[OrderRepository](_.addAll(orders))

  def findOrderById(id: java.util.UUID): ZIO[Has[OrderRepository], RepositoryError, Order] =
    ZIO.serviceWith[OrderRepository](_.findOrderById(id))

  def countAllOrders(): ZIO[Has[OrderRepository], RepositoryError, Int] =
    ZIO.serviceWith[OrderRepository](_.countAllOrders())

  def removeAll(): ZIO[Has[OrderRepository], RepositoryError, Int] =
    ZIO.serviceWith[OrderRepository](_.removeAll())

}
