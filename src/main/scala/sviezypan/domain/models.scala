package sviezypan.domain

import java.util.UUID
import java.time.LocalDate
import zio.json._

final case class Order(
    id: UUID,
    customerId: UUID,
    date: LocalDate,
  )

object Order {
  implicit val encoder: JsonEncoder[Order] = DeriveJsonEncoder.gen[Order]
  implicit val decoder: JsonDecoder[Order] = DeriveJsonDecoder.gen[Order]
}

final case class Customer(
    id: UUID,
    fname: String,
    lname: String,
    verified: Boolean,
    dateOfBirth: LocalDate,
  )

object Customer {
  implicit val encoder: JsonEncoder[Customer] = DeriveJsonEncoder.gen[Customer]
  implicit val decoder: JsonDecoder[Customer] = DeriveJsonDecoder.gen[Customer]
}

final case class Product(
    id: UUID,
    name: String,
    description: String,
    imageUrl: String,
  )

final case class ProductPrice(
    id: UUID,
    effective: LocalDate,
    price: Double,
  )

final case class OrderDetail(
    orderId: UUID,
    productId: UUID,
    quantity: Int,
    unitPrice: Double,
  )

object OrderDetail {
  implicit val encoder: JsonEncoder[OrderDetail] = DeriveJsonEncoder.gen[OrderDetail]
}

final case class CustomerWithOrderDate(
    firstName: String,
    lastName: String,
    orderDate: LocalDate,
  )

object CustomerWithOrderDate {
  implicit val encoder: JsonEncoder[CustomerWithOrderDate] = DeriveJsonEncoder.gen[CustomerWithOrderDate]
}

final case class CustomerWrapper(customers: List[CustomerWithOrderDate])

object CustomerWrapper {
  implicit val encoder: JsonEncoder[CustomerWrapper] = DeriveJsonEncoder.gen[CustomerWrapper]
}

final case class CustomerWithOrderNumber(
    firstName: String,
    lastName: String,
    count: Long,
  )

object CustomerWithOrderNumber {
  implicit val encoder: JsonEncoder[CustomerWithOrderNumber] = DeriveJsonEncoder.gen[CustomerWithOrderNumber]
}

final case class CustomerCountWrapper(customers: List[CustomerWithOrderNumber])

object CustomerCountWrapper {
  implicit val encoder: JsonEncoder[CustomerCountWrapper] = DeriveJsonEncoder.gen[CustomerCountWrapper]
}

//TODO do it right
sealed trait DomainError extends Throwable 

object DomainError {
  final case class RepositoryError(cause: Throwable) extends DomainError
  final case class BusinessError(message: String) extends DomainError
  final case class ConfigError(e: Exception) extends DomainError
  final case class ValidationError(message: String) extends DomainError
}
