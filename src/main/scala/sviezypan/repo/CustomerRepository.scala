package sviezypan.repo

import zio.stream._
import sviezypan.domain._
import sviezypan.domain.AppError.RepositoryError
import zio._

import java.util.UUID

trait CustomerRepository {

  def findAll(): ZStream[Any, RepositoryError, Customer]

  def findById(id: UUID): ZIO[Any, RepositoryError, Customer]

  def add(customer: Customer): ZIO[Any, RepositoryError, Unit]

  def add(customer: List[Customer]): ZIO[Any, RepositoryError, Int]

  def removeAll(): ZIO[Any, RepositoryError, Int]
}

object CustomerRepository {
  def findAll(): ZStream[CustomerRepository, RepositoryError, Customer] =
    ZStream.serviceWithStream[CustomerRepository](_.findAll())

  def findById(id: UUID): ZIO[CustomerRepository, RepositoryError, Customer] =
    ZIO.serviceWithZIO[CustomerRepository](_.findById(id))

  def add(
      customer: Customer
  ): ZIO[CustomerRepository, RepositoryError, Unit] =
    ZIO.serviceWithZIO[CustomerRepository](_.add(customer))

  def add(
      customer: List[Customer]
  ): ZIO[CustomerRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[CustomerRepository](_.add(customer))

  def removeAll(): ZIO[CustomerRepository, RepositoryError, Int] =
    ZIO.serviceWithZIO[CustomerRepository](_.removeAll())
}
