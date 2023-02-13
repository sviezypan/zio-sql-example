package sviezypan.healthcheck

import zio._
import zio.http._
import zio.http.model._

object Healthcheck {
  val expose: HttpApp[Any, Response] = Http.collectZIO {
    case Method.GET -> !! / "health" =>
      ZIO.succeed(Response.status(Status.Ok))
  }
}
