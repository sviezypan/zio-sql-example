package sviezypan.domain

import java.util.UUID
import java.time.LocalDate

final case class Order(
    id: UUID,
    customerId: UUID,
    orderDate: LocalDate
)

final case class Customer(
    id: UUID,
    firstName: String,
    lastName: String,
    verified: Boolean,
    dob: LocalDate
)

final case class Product(
    id: UUID,
    name: String,
    description: String,
    imageUrl: String
)

final case class ProductPrice(
    id: UUID,
    effective: LocalDate,
    price: Double
)

final case class OrderDetail(
    orderId: UUID,
    productId: UUID,
    quantity: Int,
    unitPrice: Double
)

final case class CustomerWithOrderDate(
      firstName: String,
      lastName: String,
      orderDate: LocalDate
)

final case class CustomerWithOrderNumber(
      firstName: String,
      lastName: String,
      count: Long
)

sealed trait AppError extends Throwable

object AppError {
  final case class RepositoryError(cause: Throwable) extends AppError
  final case class DecodingError(message: String) extends AppError
}