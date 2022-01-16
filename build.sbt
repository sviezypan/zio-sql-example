//depending on snapshot as zio-sql is not yet released
val zioSqlVersion = "0.0.0+965-8f6871e5-SNAPSHOT"

val zioVersion = "1.0.12"
val zioHttpVersion = "1.0.0.0-RC17"
val zioJsonVersion = "0.2.0-M3"
val zioLoggingVersion = "0.5.14"
val logbackVersion = "1.2.7"
val testcontainersVersion = "1.16.2"
val testcontainersScalaVersion = "0.39.12"
val zioConfigVersion = "1.0.10"
val zioMagicVersion = "0.3.11"
val zioSchemaVersion = "0.1.4"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        name := "zio-sql-example",
        organization := "sviezypan",
        version := "0.0.1",
        scalaVersion := "2.13.7"
      )
    ),
    resolvers +=
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    name := "zio-sql-example",
    libraryDependencies ++= Seq(
      //core
      "dev.zio" %% "zio" % zioVersion,
      "io.github.kitlangton" %% "zio-magic" % zioMagicVersion,
      //sql
      "dev.zio" %% "zio-sql-postgres" % zioSqlVersion,
      //http
      "io.d11" %% "zhttp" % zioHttpVersion,
      //config
      "dev.zio" %% "zio-config" % zioConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
      "dev.zio" %% "zio-config-magnolia" % zioConfigVersion,
      "dev.zio" %% "zio-schema" % zioSchemaVersion,
      "dev.zio" %% "zio-schema-derivation" % zioSchemaVersion,
      //logging
      "dev.zio" %% "zio-logging" % zioLoggingVersion,
      "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      //json
      "dev.zio" %% "zio-json" % zioJsonVersion,
      // test dependencies
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "dev.zio" %% "zio-test-junit" % zioVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % testcontainersScalaVersion % Test,
      "org.testcontainers" % "testcontainers" % testcontainersVersion % Test,
      "org.testcontainers" % "database-commons" % testcontainersVersion % Test,
      "org.testcontainers" % "postgresql" % testcontainersVersion % Test,
      "org.testcontainers" % "jdbc" % testcontainersVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .enablePlugins(JavaAppPackaging)
