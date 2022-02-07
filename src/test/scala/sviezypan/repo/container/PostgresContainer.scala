package sviezypan.repo.postgresql

import com.dimafeng.testcontainers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import zio._
import zio.sql.ConnectionPoolConfig
import java.util.Properties

object PostgresContainer {

  def make(
      imageName: String = "postgres:alpine"
    ) =
    ZManaged.acquireReleaseWith {
      ZIO.attemptBlocking {
        val c = new PostgreSQLContainer(
          dockerImageNameOverride = Option(imageName).map(DockerImageName.parse)
        ).configure { a =>
          a.withInitScript("init.sql")
          ()
        }
        c.start()
        c
      }
    } { container =>
      ZIO.attemptBlocking(container.stop()).orDie
    }.toLayer

  val connectionPoolConfigLayer: ZLayer[PostgreSQLContainer, Throwable, ConnectionPoolConfig] = {
    def connProperties(user: String, password: String): Properties = {
      val props = new Properties
      props.setProperty("user", user)
      props.setProperty("password", password)
      props
    }

    (for {
        c <- ZIO.service[PostgreSQLContainer]
        container = c.container
      } yield (ConnectionPoolConfig(
        container.getJdbcUrl(),
        connProperties(container.getUsername(), container.getPassword()),
      ))).toLayer
  }
}