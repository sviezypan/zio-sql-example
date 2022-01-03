package sviezypan.healthcheck

import zio._
import zhttp.http._

object Healthcheck {
  //just to verify is server is returning
  val expose: HttpApp[Any, Throwable] = HttpApp.collectM {
    case Method.GET -> Root / "health" =>
      ZIO.succeed(Response.status(Status.OK))
  }
}
