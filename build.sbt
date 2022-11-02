import Dependencies._

val scala3Version = "3.2.0"

lazy val root = project
  .in(file("."))
  .settings(
    name         := "nastenka",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      "-deprecation"
    ),
    libraryDependencies ++= distributionDependencies ++ testDependencies.map(_ % Test),
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    ThisBuild / dynverSeparator := "-",
    dockerBaseImage             := "adoptopenjdk/openjdk11:jre-11.0.10_9-alpine",
    dockerExposedPorts ++= Seq(8080),
    dockerAliases += dockerAlias.value.withTag(Option("latest"))
  )

enablePlugins(JavaAppPackaging, DockerPlugin, AshScriptPlugin)
