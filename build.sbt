import Dependencies._
import scala.sys.process.Process

val scala3Version = "3.2.0"

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
      zioInteropCats
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
    jibBaseImage    := "adoptopenjdk/openjdk11:jre-11.0.10_9-alpine",
    jibOrganization := "johnspade",
    jibName         := "nastenka",
    jibRegistry     := "ghcr.io"
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

val buildFrontend = taskKey[Unit]("Build frontend")
buildFrontend := {
  (frontend / Compile / fullLinkJS).value
  val yarnInstallExit = Process(
    "yarn" :: "install" :: "--immutable" :: "--immutable-cache" :: "--check-cache" :: Nil,
    cwd = baseDirectory.value / "frontend"
  ).run().exitValue()
  if (yarnInstallExit > 0) {
    throw new IllegalStateException(s"yarn install failed. See above for reason")
  }

  val buildExit = Process("yarn" :: "build" :: Nil, cwd = baseDirectory.value / "frontend").run().exitValue()
  if (buildExit > 0) {
    throw new IllegalStateException(s"Building frontend failed. See above for reason")
  }

  IO.copyDirectory(
    baseDirectory.value / "frontend" / "dist",
    baseDirectory.value / "api" / "target" / s"scala-$scala3Version" / "classes" / "static"
  )
}

addCommandAlias("validate", "scalafmtCheck;Test / scalafmtCheck;test")
addCommandAlias("buildDockerContainer", ";clean;compile;buildFrontend;server/jibDockerBuild")
