package sviezypan.healthcheck

import zio._
import zhttp.http._

object Healthcheck {
  val expose: HttpApp[Any, Throwable] = Http.collectZIO {
    case Method.GET -> !! / "health" =>
      ZIO.succeed(Response.status(Status.OK))
  }
}
