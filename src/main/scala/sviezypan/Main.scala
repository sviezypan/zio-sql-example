package sviezypan

import zio._
import zio.http._
import sviezypan.config.ServerConfig
import sviezypan.api.HttpRoutes
import sviezypan.healthcheck.Healthcheck
import sviezypan.repo._
import sviezypan.service.QueryServiceImpl
import zio.sql.ConnectionPool

object Main extends ZIOAppDefault {

  def run =
    zio.http.Server.serve(HttpRoutes.app ++ Healthcheck.expose)
      .provide(
        ServerConfig.layer,
        ZLayer.fromZIO(zio.config.getConfig[ServerConfig]).flatMap(c => zio.http.ServerConfig.live(http.ServerConfig.default.port(c.get.port))),
        Server.live,
        OrderRepositoryImpl.live,
        CustomerRepositoryImpl.live,
        QueryServiceImpl.live,
        sviezypan.config.DbConfig.layer,
        ConnectionPool.live,
        sviezypan.config.DbConfig.connectionPoolConfig
      )
}
