package sviezypan

import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio._
import zio.config._
import sviezypan.config._
import sviezypan.api.HttpRoutes
import sviezypan.healthcheck.Healthcheck
import sviezypan.repo._
import sviezypan.service.QueryServiceImpl
import zio.sql.ConnectionPool

object Main extends ZIOAppDefault {

  def run =
    getConfig[ServerConfig]
      .map(config =>
        Server.port(config.port) ++
          Server.app(
            HttpRoutes.app ++
              Healthcheck.expose
          )
      )
      .flatMap(_.start)
      .provide(
        ServerConfig.layer,
        ServerChannelFactory.auto,
        EventLoopGroup.auto(),
        OrderRepositoryImpl.live,
        CustomerRepositoryImpl.live,
        QueryServiceImpl.live,
        DbConfig.layer,
        ConnectionPool.live,
        DbConfig.connectionPoolConfig
      )
}
