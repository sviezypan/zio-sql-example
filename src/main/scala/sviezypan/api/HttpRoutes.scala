package sviezypan.api

import zhttp.http._
import zio._
import zio.stream._
import zio.json._
import zio.logging._
import java.util.UUID
import sviezypan.domain._
import sviezypan.repo._

object HttpRoutes {

  val app
      : HttpApp[Has[OrderRepository] with Has[CustomerRepository] with Logging, Throwable] =
    HttpApp.collectM {
      case Method.GET -> Root / "orders" / "count" =>
        OrderRepository
          .countAllOrders()
          .either
          .map {
            case Right(count) => Response.jsonString(s"{\"count\": \"${count.toString()}\"}")
            case Left(_)      => Response.status(Status.INTERNAL_SERVER_ERROR)
          }

      case Method.GET -> Root / "customers" / "orders" / "join" =>
        OrderRepository
          .findAllWithNames()
          .runCollect
          .map(chunk => CustomerWrapper(chunk.toList))
          .either
          .map {
            case Right(customers) => Response.jsonString(customers.toJson)
            case Left(_)      => Response.status(Status.INTERNAL_SERVER_ERROR)
          }

      case Method.GET -> Root / "customers" / "orders" / "latest-date" =>
        CustomerRepository
          .findAllWithLatestOrder()
          .runCollect
          .map(chunk => CustomerWrapper(chunk.toList))
          .either
          .map {
            case Right(customers) => Response.jsonString(customers.toJson)
            case Left(_)      => Response.status(Status.INTERNAL_SERVER_ERROR)
          }

      case Method.GET -> Root / "customers" / "orders" / "count" =>
        CustomerRepository
          .findAllWithCountOfOrders()
          .runCollect
          .map(chunk => CustomerCountWrapper(chunk.toList))
          .either
          .map {
            case Right(customers) => Response.jsonString(customers.toJson)
            case Left(_)      => Response.status(Status.INTERNAL_SERVER_ERROR)
          }

      case Method.GET -> Root / "customers" =>
        //TODO what is the right way to stream response to xzio-http ???
        ZIO.succeed(
          Response.HttpResponse(
            status = Status.OK,
            headers = List(Header.contentTypeJson),
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
        //TODO what is the right way to stream response to xzio-http ???
        ZIO.succeed(
          Response.HttpResponse(
            status = Status.OK,
            headers = List(Header.contentTypeJson),
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
            case Left(e)         => Response.text(e.getMessage())
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
