package ru.johnspade.nastenka

import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*
import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.MapHasAsJava
import io.getquill.context.ZioJdbc.DataSourceLayer

object Main extends ZIOAppDefault:
  private val dataSourceLive =
    ZLayer {
      ZIO.service[DbConfig].map { dbConfig =>
        val dbConfigMap = Map(
          "dataSource.user"     -> dbConfig.user,
          "dataSource.password" -> dbConfig.password,
          "dataSource.url"      -> dbConfig.url,
          "dataSourceClassName" -> "org.postgresql.ds.PGSimpleDataSource"
        )
        val typesafeConfig = ConfigFactory.parseMap(dbConfigMap.asJava)
        DataSourceLayer.fromConfig(typesafeConfig).orDie
      }
    }.flatten
  private val postgresLive = Quill.Postgres.fromNamingStrategy(SnakeCase)

  private val program =
    for
      _ <- FlywayMigration.migrate
      _ <- ZIO.serviceWithZIO[NastenkaServer](_.start)
    yield ()

  def run: Task[Unit] =
    program.provide(
      NastenkaServer.layer,
      InvestigationRoutes.layer,
      DbConfig.live,
      dataSourceLive,
      postgresLive,
      InvestigationRepositoryLive.layer
    )
