package sviezypan.api

import zio._
import zio.json._
import sviezypan.domain._

object Protocol {
  final case class Orders(orders: Chunk[Order])

  final case class Customers(customers: Chunk[Customer])

  final case class CustomerWrapper(customers: List[CustomerWithOrderDate])

  final case class CustomerCountWrapper(
      customers: List[CustomerWithOrderNumber]
  )

  implicit val customerEncoder: JsonEncoder[Customer] = DeriveJsonEncoder.gen[Customer]
  implicit val customerDecoder: JsonDecoder[Customer] = DeriveJsonDecoder.gen[Customer]

  implicit val orderEncoder: JsonEncoder[Order] = DeriveJsonEncoder.gen[Order]
  implicit val orderDecoder: JsonDecoder[Order] = DeriveJsonDecoder.gen[Order]
  
  implicit val ordersEncoder: JsonEncoder[Orders] =
    DeriveJsonEncoder.gen[Orders]
    
  implicit val orderDetailEncoder: JsonEncoder[OrderDetail] =
      DeriveJsonEncoder.gen[OrderDetail]

  implicit val customerWithOrderDateEncoder: JsonEncoder[CustomerWithOrderDate] =
    DeriveJsonEncoder.gen[CustomerWithOrderDate]

  implicit val customerWrapperEncoder: JsonEncoder[CustomerWrapper] =
    DeriveJsonEncoder.gen[CustomerWrapper]

  implicit val customerWithOrderNumberEncoder: JsonEncoder[CustomerWithOrderNumber] =
    DeriveJsonEncoder.gen[CustomerWithOrderNumber]

  implicit val customerCountWrapperEncoder: JsonEncoder[CustomerCountWrapper] =
    DeriveJsonEncoder.gen[CustomerCountWrapper]

  implicit val customersEncoder: JsonEncoder[Customers] =
    DeriveJsonEncoder.gen[Customers]
}
