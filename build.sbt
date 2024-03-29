import Dependencies._
import scala.sys.process.Process

val scala3Version = "3.3.0"

ThisBuild / organization    := "ru.johnspade"
ThisBuild / scalaVersion    := scala3Version
ThisBuild / dynverSeparator := "-"

Global / onChangedBuildSource := ReloadOnSourceChanges

val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "utf8"
  ),
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

lazy val root = project
  .in(file("."))
  .settings(name := "nastenka")
  .aggregate(shared, persistenceShared, api, inbox, telegram, email, server, frontend)

lazy val shared = project
  .in(file("shared"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    libraryDependencies ++= Seq(
      zioJson
    )
  )

lazy val persistenceShared = project
  .in(file("persistence-shared"))
  .settings(commonSettings)
  .dependsOn(shared)
  .settings(
    libraryDependencies ++= Seq(
      zio,
      quill,
      postgresql
    )
  )

lazy val api = project
  .in(file("api"))
  .dependsOn(shared, persistenceShared)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      zio,
      zioConfig,
      zioJson,
      zioHttp,
      zioLogging,
      logback,
      flyway,
      postgresql,
      quill,
      ducktape,
      zioTest,
      zioTestSbt
    )
  )

lazy val inbox = project
  .in(file("inbox"))
  .dependsOn(shared, persistenceShared)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      zio,
      zioConfig,
      zioS3,
      postgresql,
      quill,
      ducktape
    )
  )

lazy val telegram = project
  .in(file("sources/telegram"))
  .dependsOn(inbox)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      zio,
      zioConfig,
      telegramiumCore,
      telegramiumHigh,
      zioInteropCats,
      sttpClientZio,
      jsoup
    )
  )

lazy val email = project
  .in(file("sources/email"))
  .dependsOn(inbox)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      zio,
      zioConfig,
      zioNio,
      zioInteropCats,
      emilCommon,
      emilJavamail,
      logback
    )
  )

lazy val server = project
  .in(file("server"))
  .dependsOn(api, inbox, telegram, email, persistenceShared)
  .settings(commonSettings)
  .settings(
    name            := "nastenka-server",
    jibBaseImage    := "eclipse-temurin:17.0.7_7-jre",
    jibOrganization := "johnspade",
    jibName         := "nastenka-backend",
    jibRegistry     := "ghcr.io",
    jibLabels       := Map("org.opencontainers.image.source" -> "https://github.com/johnspade/nastenka")
  )

lazy val frontend = project
  .in(file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(shared)
  .settings(commonSettings)
  .settings(
    name := "nastenka-frontend",
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    libraryDependencies ++= Seq(
      "com.raquo"                     %%% "laminar"  % V.laminar,
      "com.raquo"                     %%% "waypoint" % V.waypoint,
      "dev.zio"                       %%% "zio-json" % V.zioJson,
      "com.softwaremill.sttp.client3" %%% "core"     % V.sttpClient
    )
  )

addCommandAlias("validate", ";compile;Test/compile;scalafmtCheck;Test/scalafmtCheck;test")
addCommandAlias("publishDockerContainer", ";server/jibImageBuild")
