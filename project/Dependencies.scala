import sbt.librarymanagement.syntax._

object Dependencies {
  object V {
    val zio        = "2.0.3"
    val zioConfig  = "3.0.2"
    val zioHttp    = "2.0.0-RC11"
    val zioJson    = "0.3.0"
    val zioQuill   = "4.6.0"
    val zioLogging = "2.1.1"
    val logback    = "1.2.7"
    val flyway     = "8.0.5"
    val postgresql = "42.3.1"
    val ducktape   = "0.1.0-RC2"
  }

  val distributionDependencies = Seq(
    "dev.zio"           %% "zio"               % V.zio,
    "dev.zio"           %% "zio-config"        % V.zioConfig,
    "io.d11"            %% "zhttp"             % V.zioHttp,
    "dev.zio"           %% "zio-json"          % V.zioJson,
    "dev.zio"           %% "zio-logging-slf4j" % V.zioLogging,
    "ch.qos.logback"     % "logback-classic"   % V.logback,
    "org.flywaydb"       % "flyway-core"       % V.flyway,
    "org.postgresql"     % "postgresql"        % V.postgresql,
    "io.getquill"       %% "quill-jdbc-zio"    % V.zioQuill,
    "io.github.arainko" %% "ducktape"          % V.ducktape
  )

  val testDependencies = Seq(
    "dev.zio" %% "zio-test"     % V.zio,
    "dev.zio" %% "zio-test-sbt" % V.zio
  )
}
