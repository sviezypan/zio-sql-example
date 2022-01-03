package sviezypan

import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio._
import zio.config._
import zio.logging._
import io.regec.api._
import sviezypan.config.configuration._
import io.regec.healthcheck._
import io.regec.repo._
import sviezypan.api.HttpRoutes
import sviezypan.healthcheck.Healthcheck
import sviezypan.repo.{CustomerRepositoryLive, OrderRepositoryLive, PriceRepositoryLive}
import zio.magic._
import zio.sql.ConnectionPool

object Main extends App {

  val loggingEnv = 
    Logging.console(LogLevel.Info, LogFormat.ColoredLogFormat()) >>>
        Logging.withRootLoggerName("zio-sql-example")

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    getConfig[ServerConfig]
      .map(config => Server.port(config.port) ++
            Server.app(
              HttpRoutes.app +++
                Healthcheck.expose
            ))
      .flatMap(_.make.useForever)
      .injectCustom(
        ServerConfig.layer,
        ServerChannelFactory.auto,
        EventLoopGroup.auto(),
        PriceRepositoryLive.layer,
        OrderRepositoryLive.layer,
        CustomerRepositoryLive.layer,
        loggingEnv,
        ConnectionPool.live,
        DbConfig.layer,
        DbConfig.connectionPoolConfig
      )
      .exitCode
}
