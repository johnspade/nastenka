import sbt.librarymanagement.syntax._

object Dependencies {
  object V {
    val zio         = "2.0.2"
    val zioConfig   = "3.0.2"
    val zioHttp     = "2.0.0-RC11"
    val zioJson     = "0.3.0"
    val zioQuill    = "4.6.0"
    val zioLogging  = "2.1.1"
    val zioNio      = "2.0.0"
    val zioS3       = "0.4.2.1"
    val zioCats     = "3.3.0+10-274fad37-SNAPSHOT"
    val logback     = "1.2.7"
    val flyway      = "8.0.5"
    val postgresql  = "42.3.1"
    val ducktape    = "0.1.0-RC2"
    val telegramium = "7.62.1"
    val laminar     = "0.14.5"
    val waypoint    = "0.5.0"
    val sttpClient  = "3.8.3"
    val emil        = "0.11.2"
  }

  val zio        = "dev.zio" %% "zio"               % V.zio
  val zioStreams = "dev.zio" %% "zio-streams"       % V.zio
  val zioConfig  = "dev.zio" %% "zio-config"        % V.zioConfig
  val zioHttp    = "io.d11"  %% "zhttp"             % V.zioHttp
  val zioJson    = "dev.zio" %% "zio-json"          % V.zioJson
  val zioLogging = "dev.zio" %% "zio-logging-slf4j" % V.zioLogging
  val zioNio = "dev.zio" %% "zio-nio" % V.zioNio exclude ("org.scala-lang.modules", "scala-collection-compat_2.13")
  val zioS3  = "dev.zio" %% "zio-s3"  % V.zioS3 exclude ("org.scala-lang.modules", "scala-collection-compat_2.13")
  val logback         = "ch.qos.logback"         % "logback-classic"  % V.logback
  val flyway          = "org.flywaydb"           % "flyway-core"      % V.flyway
  val postgresql      = "org.postgresql"         % "postgresql"       % V.postgresql
  val quill           = "io.getquill"           %% "quill-jdbc-zio"   % V.zioQuill
  val ducktape        = "io.github.arainko"     %% "ducktape"         % V.ducktape
  val telegramiumCore = "io.github.apimorphism" %% "telegramium-core" % V.telegramium
  val telegramiumHigh = "io.github.apimorphism" %% "telegramium-high" % V.telegramium
  val zioInteropCats  = "dev.zio"               %% "zio-interop-cats" % V.zioCats
  val emilCommon      = "com.github.eikek"      %% "emil-common"      % V.emil
  val emilJavamail    = "com.github.eikek"      %% "emil-javamail"    % V.emil

  val zioTest    = "dev.zio" %% "zio-test"     % V.zio % Test
  val zioTestSbt = "dev.zio" %% "zio-test-sbt" % V.zio % Test
}
