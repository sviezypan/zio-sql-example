package sviezypan.healthcheck

import zio.test._
import zio.test.Assertion._
import zhttp.test._
import zhttp.http._
import io.regec.healthcheck._
import zhttp.http.HttpData.Empty

object HealthcheckSpec extends DefaultRunnableSpec {
  def spec = suite("http") (
    suite("health check")(
      testM("ok status") {
        val actual = Healthcheck.expose(Request(Method.GET -> URL(Root / "health")))
        val expected: Response.HttpResponse[Any, Nothing] = Response.HttpResponse(Status.OK, List(), Empty)
        assertM(actual)(equalTo(expected))
      }
    )
  )
}