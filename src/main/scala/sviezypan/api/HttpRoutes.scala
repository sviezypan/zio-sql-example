package sviezypan.api

import zhttp.http._
import zio._
import zio.json._
import sviezypan.domain._
import sviezypan.repo._
import sviezypan.service._
import sviezypan.api.Protocol._

object HttpRoutes {

  val app: HttpApp[
    OrderRepository with CustomerRepository with QueryService,
    Throwable
  ] =
    Http.collectZIO {
      case Method.GET -> !! / "orders" / "count" =>
        OrderRepository
          .countAll()
          .either
          .map {
            case Right(count) =>
              Response.json(s"{\"count\": \"${count.toString()}\"}")
            case Left(_) => Response.status(Status.INTERNAL_SERVER_ERROR)
          }

      case Method.GET -> !! / "customers" / "orders" / "join" =>
        QueryService
          .findAllWithNames()
          .runCollect
          .map(chunk => CustomerWrapper(chunk.toList))
          .either
          .map {
            case Right(customers) => Response.json(customers.toJson)
            case Left(_) => Response.status(Status.INTERNAL_SERVER_ERROR)
          }

      case Method.GET -> !! / "customers" / "orders" / "latest-date" =>
        QueryService
          .findAllWithLatestOrder()
          .runCollect
          .map(chunk => CustomerWrapper(chunk.toList))
          .either
          .map {
            case Right(customers) => Response.json(customers.toJson)
            case Left(_) => Response.status(Status.INTERNAL_SERVER_ERROR)
          }

      case Method.GET -> !! / "customers" / "orders" / "count" =>
        QueryService
          .findAllWithCountOfOrders()
          .runCollect
          .map(chunk => CustomerCountWrapper(chunk.toList))
          .either
          .map {
            case Right(customers) => Response.json(customers.toJson)
            case Left(_) => Response.status(Status.INTERNAL_SERVER_ERROR)
          }

      case Method.GET -> !! / "customers" =>
        CustomerRepository
          .findAll()
          .runCollect
          .map(ch => Response.json(ch.toJson))

      case Method.GET -> !! / "orders" =>
        OrderRepository
          .findAll()
          .runCollect
          .map(ch => Response.json(ch.toJson))

      case req @ Method.POST -> !! / "customers" =>
        (for {
          body <- req.bodyAsString
            .flatMap(request =>
              ZIO
                .fromEither(request.fromJson[Customer])
                .mapError(e => new Throwable(e))
            )
            .mapError(e => AppError.DecodingError(e.getMessage()))
            .tapError(e => ZIO.logInfo(s"Unparseable body ${e}"))
          _ <- CustomerRepository.add(body)
        } yield ()).either.map {
          case Right(_) => Response.status(Status.CREATED)
          case Left(_)  => Response.status(Status.BAD_REQUEST)
        }

      case Method.GET -> !! / "customers" / zhttp.http.uuid(id) =>
        CustomerRepository
          .findById(id)
          .either
          .map {
            case Right(customer) => Response.json(customer.toJson)
            case Left(e)         => Response.text(e.getMessage())
          }

      case req @ Method.POST -> !! / "orders" =>
        (for {
          body <- req.bodyAsString
            .flatMap(request =>
              ZIO
                .fromEither(request.fromJson[Order])
                .mapError(e => new Throwable(e))
            )
            .mapError(e => AppError.DecodingError(e.getMessage()))
            .tapError(e => ZIO.logInfo(s"Unparseable body ${e}"))
          _ <- OrderRepository.add(body)
        } yield ()).either.map {
          case Right(_) => Response.status(Status.CREATED)
          case Left(_)  => Response.status(Status.BAD_REQUEST)
        }

      case Method.GET -> !! / "orders" / zhttp.http.uuid(id) =>
        OrderRepository
          .findById(id)
          .either
          .map {
            case Right(order) => Response.json(order.toJson)
            case Left(e)      => Response.text(e.getMessage())
          }
    }
}
