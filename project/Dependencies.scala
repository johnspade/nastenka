import sbt.librarymanagement.syntax._

object Dependencies {
  object V {
    val zio         = "2.0.15"
    val zioConfig   = "4.0.0-RC16"
    val zioHttp     = "3.0.0-RC2"
    val zioJson     = "0.5.0"
    val zioQuill    = "4.6.0.1"
    val zioLogging  = "2.1.13"
    val zioNio      = "2.0.1"
    val zioS3       = "0.4.2.4"
    val zioCats     = "23.0.0.6"
    val logback     = "1.4.8"
    val flyway      = "9.20.0"
    val postgresql  = "42.6.0"
    val ducktape    = "0.1.8"
    val telegramium = "8.67.0"
    val laminar     = "15.0.1"
    val waypoint    = "6.0.0"
    val sttpClient  = "3.8.15"
    val emil        = "0.13.0"
  }

  val zio        = "dev.zio" %% "zio"               % V.zio
  val zioStreams = "dev.zio" %% "zio-streams"       % V.zio
  val zioConfig  = "dev.zio" %% "zio-config"        % V.zioConfig
  val zioHttp    = "dev.zio" %% "zio-http"          % V.zioHttp
  val zioJson    = "dev.zio" %% "zio-json"          % V.zioJson
  val zioLogging = "dev.zio" %% "zio-logging-slf4j" % V.zioLogging
  val zioNio = "dev.zio" %% "zio-nio" % V.zioNio exclude ("org.scala-lang.modules", "scala-collection-compat_2.13")
  val zioS3  = "dev.zio" %% "zio-s3"  % V.zioS3 exclude ("org.scala-lang.modules", "scala-collection-compat_2.13")
  val logback         = "ch.qos.logback"                 % "logback-classic"  % V.logback
  val flyway          = "org.flywaydb"                   % "flyway-core"      % V.flyway
  val postgresql      = "org.postgresql"                 % "postgresql"       % V.postgresql
  val quill           = "io.getquill"                   %% "quill-jdbc-zio"   % V.zioQuill
  val ducktape        = "io.github.arainko"             %% "ducktape"         % V.ducktape
  val telegramiumCore = "io.github.apimorphism"         %% "telegramium-core" % V.telegramium
  val telegramiumHigh = "io.github.apimorphism"         %% "telegramium-high" % V.telegramium
  val zioInteropCats  = "dev.zio"                       %% "zio-interop-cats" % V.zioCats
  val emilCommon      = "com.github.eikek"              %% "emil-common"      % V.emil
  val emilJavamail    = "com.github.eikek"              %% "emil-javamail"    % V.emil
  val sttpClientZio   = "com.softwaremill.sttp.client3" %% "zio"              % V.sttpClient

  val zioTest    = "dev.zio" %% "zio-test"     % V.zio % Test
  val zioTestSbt = "dev.zio" %% "zio-test-sbt" % V.zio % Test
}
