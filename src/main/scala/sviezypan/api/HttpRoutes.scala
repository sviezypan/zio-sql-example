package sviezypan.api

import zio.http._
import zio._
import zio.json._
import sviezypan.domain._
import sviezypan.repo._
import sviezypan.service._
import sviezypan.api.Protocol._
import zio.http.model._

object HttpRoutes {

  val app: HttpApp[
    OrderRepository with CustomerRepository with QueryService,
    Response
  ] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "orders" / "count" =>
        OrderRepository
          .countAll()
          .either
          .map {
            case Right(count) =>
              Response.json(s"{\"count\": \"${count.toString()}\"}")
            case Left(_) => Response.status(Status.InternalServerError)
          }

      case Method.GET -> !! / "customers" / "orders" / "join" =>
        QueryService
          .findAllWithNames()
          .runCollect
          .map(chunk => CustomerWrapper(chunk.toList))
          .either
          .map {
            case Right(customers) => Response.json(customers.toJson)
            case Left(_) => Response.status(Status.InternalServerError)
          }

      case Method.GET -> !! / "customers" / "orders" / "latest-date" =>
        QueryService
          .findAllWithLatestOrder()
          .runCollect
          .map(chunk => CustomerWrapper(chunk.toList))
          .either
          .map {
            case Right(customers) => Response.json(customers.toJson)
            case Left(_) => Response.status(Status.InternalServerError)
          }

      case Method.GET -> !! / "customers" / "orders" / "count" =>
        QueryService
          .findAllWithCountOfOrders()
          .runCollect
          .map(chunk => CustomerCountWrapper(chunk.toList))
          .either
          .map {
            case Right(customers) => Response.json(customers.toJson)
            case Left(_) => Response.status(Status.InternalServerError)
          }

      case Method.GET -> !! / "customers" =>
        CustomerRepository
          .findAll()
          .runCollect
          .either
          .map {
            case Right(ch) => Response.json(ch.toJson)
            case Left(_) => Response.status(Status.InternalServerError)
          }

      case Method.GET -> !! / "orders" =>
        OrderRepository
          .findAll()
          .runCollect
          .either
          .map {
            case Right(ch) => Response.json(ch.toJson)
            case Left(_) => Response.status(Status.InternalServerError)
          }

      case req @ Method.POST -> !! / "customers" =>
        (for {
          body <- req.body.asString
            .flatMap(request =>
              ZIO
                .fromEither(request.fromJson[Customer])
                .mapError(e => new Throwable(e))
            )
            .mapError(e => AppError.DecodingError(e.getMessage()))
            .tapError(e => ZIO.logInfo(s"Unparseable body ${e}"))
          _ <- CustomerRepository.add(body)
        } yield ()).either.map {
          case Right(_) => Response.status(Status.Created)
          case Left(_)  => Response.status(Status.BadRequest)
        }

      case Method.GET -> !! / "customers" / zio.http.uuid(id) =>
        CustomerRepository
          .findById(id)
          .either
          .map {
            case Right(customer) => Response.json(customer.toJson)
            case Left(e)         => Response.text(e.getMessage())
          }

      case req @ Method.POST -> !! / "orders" =>
        (for {
          body <- req.body.asString
            .flatMap(request =>
              ZIO
                .fromEither(request.fromJson[Order])
                .mapError(e => new Throwable(e))
            )
            .mapError(e => AppError.DecodingError(e.getMessage()))
            .tapError(e => ZIO.logInfo(s"Unparseable body ${e}"))
          _ <- OrderRepository.add(body)
        } yield ()).either.map {
          case Right(_) => Response.status(Status.Created)
          case Left(_)  => Response.status(Status.BadRequest)
        }

      case Method.GET -> !! / "orders" / zio.http.uuid(id) =>
        OrderRepository
          .findById(id)
          .either
          .map {
            case Right(order) => Response.json(order.toJson)
            case Left(e)      => Response.text(e.getMessage())
          }
    }
}
