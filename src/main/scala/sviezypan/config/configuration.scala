package sviezypan.config

import zio._
import zio.config._
import zio.config.ConfigDescriptor._
import zio.config.typesafe.TypesafeConfigSource
import com.typesafe.config.ConfigFactory
import sviezypan.domain.DomainError
import zio.config.magnolia.DeriveConfigDescriptor.descriptor
import zio.sql.ConnectionPoolConfig

import java.util.Properties

object configuration {
  final case class ServerConfig(port: Int)

  object ServerConfig {

    private val serverConfigDescription = nested("server-config") {
      int("port").default(8090)
    }.to[ServerConfig]

    val layer = IO
      .fromEither(TypesafeConfigSource.fromTypesafeConfig(ConfigFactory.defaultApplication()))
      .map(source => serverConfigDescription from source)
      .flatMap(config => ZIO.fromEither(read(config)))
      .mapError(e => DomainError.ConfigError(e))
      .toLayer
  }

  final case class DbConfig(
      host: String,
      port: String,
      dbName: String,
      url: String,
      user: String,
      password: String,
      driver: String,
      connectThreadPoolSize: Int
    )

  object DbConfig {

    val dbConfigDescriptor = nested("db-config")(descriptor[DbConfig])

    val layer = IO
      .fromEither(TypesafeConfigSource.fromTypesafeConfig(ConfigFactory.defaultApplication()))
      .map(source => dbConfigDescriptor from source)
      .flatMap(config => ZIO.fromEither(read(config)))
      .mapError(e => 
          DomainError.ConfigError(e) 
      )
      .toLayer

    val connectionPoolConfig: ZLayer[Has[DbConfig], Throwable, Has[ConnectionPoolConfig]] =
      (for {
        serverConfig <- ZIO.service[DbConfig]
      } yield (ConnectionPoolConfig(
        serverConfig.url,
        connProperties(serverConfig.user, serverConfig.password),
      ))).toLayer

    private def connProperties(user: String, password: String): Properties = {
      val props = new Properties
      props.setProperty("user", user)
      props.setProperty("password", password)
      props
    }
  }
}
