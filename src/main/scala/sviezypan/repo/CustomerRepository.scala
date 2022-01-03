package sviezypan.repo

import zio.stream._
import sviezypan.domain.DomainError._
import io.regec.domain._
import sviezypan.domain.{Customer, CustomerWithOrderDate, CustomerWithOrderNumber}
import sviezypan.domain.DomainError.RepositoryError
import zio._

import java.util.UUID

trait CustomerRepository {

  def findAll(): ZStream[Any, RepositoryError, Customer]

  def findById(id: UUID): ZIO[Any, RepositoryError, Customer]

  def create(customer: Customer): ZIO[Any, RepositoryError, Unit]

  def create(customer: List[Customer]): ZIO[Any, RepositoryError, Int]

  def findAllWithLatestOrder(): ZStream[Any, RepositoryError, CustomerWithOrderDate]

  def findAllWithCountOfOrders(): ZStream[Any, RepositoryError, CustomerWithOrderNumber]

  def removeAll(): ZIO[Any, RepositoryError, Int]
}

object CustomerRepository {
  def findAll(): ZStream[Has[CustomerRepository], RepositoryError, Customer] = 
    ZStream.serviceWithStream[CustomerRepository](_.findAll())

  def findById(id: UUID): ZIO[Has[CustomerRepository], RepositoryError, Customer] = 
    ZIO.serviceWith[CustomerRepository](_.findById(id))

  def create(customer: Customer): ZIO[Has[CustomerRepository], RepositoryError, Unit] = 
    ZIO.serviceWith[CustomerRepository](_.create(customer))

  def create(customer: List[Customer]): ZIO[Has[CustomerRepository], RepositoryError, Int] = 
    ZIO.serviceWith[CustomerRepository](_.create(customer))

  def findAllWithLatestOrder(): ZStream[Has[CustomerRepository], RepositoryError, CustomerWithOrderDate] = 
    ZStream.serviceWithStream[CustomerRepository](_.findAllWithLatestOrder())

  def findAllWithCountOfOrders(): ZStream[Has[CustomerRepository], RepositoryError, CustomerWithOrderNumber] = 
    ZStream.serviceWithStream[CustomerRepository](_.findAllWithCountOfOrders())

  def removeAll(): ZIO[Has[CustomerRepository], RepositoryError, Int] =
    ZIO.serviceWith[CustomerRepository](_.removeAll())
}
