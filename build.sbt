import Dependencies._

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
  .aggregate(shared, persistenceShared, api, inbox, telegram, backend, frontend)

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

lazy val backend = project
  .in(file("backend"))
  .dependsOn(api, telegram, persistenceShared)
  .settings(commonSettings)
  .enablePlugins(JavaAppPackaging, DockerPlugin, AshScriptPlugin)
  .settings(
    name            := "nastenka-backend",
    dockerBaseImage := "adoptopenjdk/openjdk11:jre-11.0.10_9-alpine",
    dockerExposedPorts ++= Seq(8080),
    dockerAliases += dockerAlias.value.withTag(Option("latest")),
    Docker / packageName := "nastenka"
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
