val zioSqlVersion = "0.0.0+937-f982c609+20220103-1522-SNAPSHOT"

val zioVersion = "1.0.12"
val zioHttpVersion = "1.0.0.0-RC17"
val zioJsonVersion = "0.2.0-M3"
val zioLoggingVersion = "0.5.14"
val logbackVersion = "1.2.7"
val testcontainersVersion      = "1.16.2"
val testcontainersScalaVersion = "0.39.12"
val zioConfigVersion = "1.0.10"
val zioMagicVersion = "0.3.11"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        name := "zio-sql-example",
        organization := "sviezypan",
        version := "0.0.1",
        scalaVersion := "2.13.7",
      )
    ),
    // TODO remove, temporary solution to find zhttp-test
    // https://github.com/dream11/zio-http/issues/321
    resolvers += "Sonatype OSS Snapshots s01" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
    name := "zio-sql-example",
    libraryDependencies ++= Seq(
      //core
      "dev.zio" %% "zio" % zioVersion,
      "io.github.kitlangton" %% "zio-magic" % zioMagicVersion,
      //sql
      "dev.zio" %% "zio-sql-postgres" % zioSqlVersion,
      //http
      "io.d11" %% "zhttp" % zioHttpVersion,
      "io.d11" %% "zhttp-test" % "1.0.0.0-RC17+37-1c8ceea7-SNAPSHOT" % Test,
      //config
      "dev.zio" %% "zio-config" % zioConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
      "dev.zio" %% "zio-config-magnolia" % zioConfigVersion,
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
      "com.dimafeng"      %% "testcontainers-scala-postgresql" % testcontainersScalaVersion % Test,
      "org.testcontainers" % "testcontainers"                  % testcontainersVersion      % Test,
      "org.testcontainers" % "database-commons"                % testcontainersVersion      % Test,
      "org.testcontainers" % "postgresql"                      % testcontainersVersion      % Test,
      "org.testcontainers" % "jdbc"                            % testcontainersVersion      % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )
  .enablePlugins(JavaAppPackaging)
