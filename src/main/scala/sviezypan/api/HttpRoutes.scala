package sviezypan.api

import zhttp.http._
import zio._
import zio.stream._
import zio.json._
import zio.logging._

import java.util.UUID
import io.regec.domain._
import sviezypan.domain.{Customer, DomainError, Order}
import sviezypan.repo.{CustomerRepository, OrderRepository, PriceRepository}

object HttpRoutes {

  val app
      : HttpApp[Has[PriceRepository] with Has[OrderRepository] with Has[CustomerRepository] with Logging, Throwable] =
    HttpApp.collectM {
      case Method.GET -> Root / "orders" / "count" =>
        OrderRepository
          .countAllOrders()
          .either
          .map {
            case Right(count) => Response.text(count.toString())
            case Left(_)      => Response.status(Status.BAD_REQUEST)
          }

      //TODO fix this
      // case Method.GET -> Root / "orders" / "above-average-price" =>
      //   PriceRepository
      //     .findOrdersWithHigherThanAvgPrice()
      //     .runCollect
      //     .map(chunk => chunk.toJson)
      //     .either
      //     .map {
      //       case Right(order) => Response.jsonString(order.toJson)
      //       case Left(_)      => Response.status(Status.BAD_REQUEST)
      //     }

      //TODO format chunks better
      case Method.GET -> Root / "customers" / "orders" / "all" =>
        OrderRepository
          .findAllWithNames()
          .runCollect
          .map(chunk => chunk.toJson)
          .either
          .map {
            case Right(order) => Response.jsonString(order.toJson)
            case Left(_)      => Response.status(Status.BAD_REQUEST)
          }

      case Method.GET -> Root / "customers" / "orders" / "latest" =>
        CustomerRepository
          .findAllWithLatestOrder()
          .runCollect
          .map(chunk => chunk.toJson)
          .either
          .map {
            case Right(value) => Response.jsonString(value.toJson)
            case Left(_)      => Response.status(Status.BAD_REQUEST)
          }

      case Method.GET -> Root / "customers" / "orders" / "count" =>
        CustomerRepository
          .findAllWithCountOfOrders()
          .runCollect
          .map(chunk => chunk.toJson)
          .either
          .map {
            case Right(value) => Response.jsonString(value.toJson)
            case Left(_)      => Response.status(Status.BAD_REQUEST)
          }

      case Method.GET -> Root / "customers" =>
        //TODO find out if this is the right way to stream response
        ZIO.succeed(
          Response.HttpResponse(
            status = Status.OK,
            headers = Nil,
            content = HttpData.fromStream(
              CustomerRepository
                .findAll()
                .flatMap(customer =>
                  Stream.fromChunk(Chunk.fromArray(customer.toJson.getBytes(HTTP_CHARSET)))
                )
            ),
          )
        )

      case Method.GET -> Root / "orders" =>
        ZIO.succeed(
          Response.HttpResponse(
            status = Status.OK,
            headers = Nil,
            content = HttpData.fromStream(
              OrderRepository
                .findAll()
                .flatMap(order =>
                  Stream.fromChunk(Chunk.fromArray(order.toJson.getBytes(HTTP_CHARSET)))
                )
            ),
          )
        )

      case req @ Method.POST -> Root / "customers" =>
        (for {
          body <- ZIO
            .fromOption(req.getBodyAsString)
            .flatMap(request => ZIO.fromEither(request.fromJson[Customer]))
            .tapError(_ => log.info(s"Unparseable body ${req.getBodyAsString}"))
          _ <- CustomerRepository.create(body)
        } yield ()).either.map {
          case Right(_) => Response.ok
          case Left(_)  => Response.status(Status.BAD_REQUEST)
        }

      case Method.GET -> Root / "customers" / id =>
        parseUUID(id)
          .flatMap(id => CustomerRepository.findById(id))
          .either
          .map {
            case Right(customer) => Response.jsonString(customer.toJson)
            case Left(_)         => Response.status(Status.NOT_FOUND)
          }

      case req @ Method.POST -> Root / "orders" =>
        (for {
          body <- ZIO
            .fromOption(req.getBodyAsString)
            .flatMap(request => ZIO.fromEither(request.fromJson[Order]))
            .tapError(_ => log.info(s"Unparseable body ${req.getBodyAsString}"))
          _ <- OrderRepository.add(body)
        } yield ()).either.map {
          case Right(_) => Response.ok
          case Left(_)  => Response.status(Status.BAD_REQUEST)
        }

      case Method.GET -> Root / "orders" / id =>
        parseUUID(id)
          .flatMap(id => OrderRepository.findOrderById(id))
          .either
          .map {
            case Right(order) => Response.jsonString(order.toJson)
            case Left(e)      => Response.text(e.getMessage())
          }

    }

  private def parseUUID(id: String): IO[DomainError.ValidationError, UUID] =
    ZIO
      .fromTry(scala.util.Try(UUID.fromString(id)))
      .mapError(_ => DomainError.ValidationError(s"$id is not a valid UUID"))

}
