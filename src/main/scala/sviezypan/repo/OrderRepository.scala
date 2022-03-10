package sviezypan.repo

import sviezypan.domain.AppError.RepositoryError
import sviezypan.domain._
import zio._
import zio.stream._
import java.util.UUID

trait OrderRepository {

  def findById(id: UUID): IO[RepositoryError, Order]

  def findAll(): ZStream[Any, RepositoryError, Order]

  def add(order: Order): IO[RepositoryError, Int]

  def add(orders: List[Order]): IO[RepositoryError, Int]

  def countAll(): IO[RepositoryError, Int]

  def removeAll(): ZIO[Any, RepositoryError, Int]
}

object OrderRepository {

  def findById(
      id: java.util.UUID
  ): ZIO[OrderRepository, RepositoryError, Order] =
    ZIO.serviceWithZIO[OrderRepository](_.findById(id))

  def findAll(): ZStream[OrderRepository, RepositoryError, Order] =
    ZStream.serviceWithStream[OrderRepository](_.findAll())

  def add(order: Order): ZIO[OrderRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[OrderRepository](_.add(order))

  def add(orders: List[Order]): ZIO[OrderRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[OrderRepository](_.add(orders))

  def countAll(): ZIO[OrderRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[OrderRepository](_.countAll())

  def removeAll(): ZIO[OrderRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[OrderRepository](_.removeAll())

}
