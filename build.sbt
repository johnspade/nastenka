import Dependencies._

val scala3Version = "3.2.0"

ThisBuild / organization    := "ru.johnspade"
ThisBuild / version         := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion    := scala3Version
ThisBuild / dynverSeparator := "-"

val sharedSettings = Seq(
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
  .aggregate(shared, persistenceShared, api, inbox, telegram, backend)

lazy val shared = project
  .in(file("shared"))
  .settings(
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
  .settings(sharedSettings)
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
  .settings(sharedSettings)
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
  .settings(sharedSettings)
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
  .settings(sharedSettings)
  .enablePlugins(JavaAppPackaging, DockerPlugin, AshScriptPlugin)
  .settings(
    dockerBaseImage := "adoptopenjdk/openjdk11:jre-11.0.10_9-alpine",
    dockerExposedPorts ++= Seq(8080),
    dockerAliases += dockerAlias.value.withTag(Option("latest")),
    packageName := "nastenka-backend"
  )
