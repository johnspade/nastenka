package ru.johnspade.nastenka.backend

import com.typesafe.config.ConfigFactory
import io.getquill.*
import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.jdbczio.Quill
import ru.johnspade.nastenka.api.ApiInvestigationRepositoryLive
import ru.johnspade.nastenka.api.ApiInvestigationServiceLive
import ru.johnspade.nastenka.api.DbConfig
import ru.johnspade.nastenka.api.FlywayMigration
import ru.johnspade.nastenka.api.InvestigationRoutes
import ru.johnspade.nastenka.api.NastenkaServer
import ru.johnspade.nastenka.email.ChromeServiceLive
import ru.johnspade.nastenka.email.EmailConfig
import ru.johnspade.nastenka.email.EmailServiceLive
import ru.johnspade.nastenka.email.EmailSourceService
import ru.johnspade.nastenka.email.EmailSourceServiceLive
import ru.johnspade.nastenka.email.PrintServiceLive
import ru.johnspade.nastenka.email.ProcessedEmailRepositoryLive
import ru.johnspade.nastenka.inbox.InboxServiceLive
import ru.johnspade.nastenka.inbox.S3Config
import ru.johnspade.nastenka.inbox.S3Live
import ru.johnspade.nastenka.persistence.InvestigationRepositoryLive
import ru.johnspade.nastenka.telegram.BotConfig
import ru.johnspade.nastenka.telegram.TelegramBot
import ru.johnspade.nastenka.telegram.TelegramBotApi
import zio.*
import zio.interop.catz.*
import zio.stream.ZSink

import scala.jdk.CollectionConverters.MapHasAsJava

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
        Quill.DataSource.fromConfig(typesafeConfig).orDie
      }
    }.flatten
  private val postgresLive =
    Quill.Postgres.fromNamingStrategy(
      CompositeNamingStrategy2[SnakeCase, PluralizedTableNames](SnakeCase, PluralizedTableNames)
    )

  private val program =
    for
      _         <- FlywayMigration.migrate
      botConfig <- ZIO.service[BotConfig]
      _ <- ZIO
        .serviceWithZIO[NastenkaServer](_.start)
        .zipPar {
          ZIO.serviceWithZIO[TelegramBot](_.start(botConfig.port, "0.0.0.0").useForever)
        }
        .zipPar {
          ZIO.serviceWithZIO[EmailSourceService](_.createStream.run(ZSink.drain))
        }
    yield ()

  def run: Task[Unit] =
    program.provide(
      NastenkaServer.layer,
      InvestigationRoutes.layer,
      DbConfig.live,
      dataSourceLive,
      postgresLive,
      InvestigationRepositoryLive.layer,
      ApiInvestigationRepositoryLive.layer,
      ApiInvestigationServiceLive.layer,
      InboxServiceLive.layer,
      BotConfig.live,
      TelegramBotApi.live,
      TelegramBot.live,
      ProcessedEmailRepositoryLive.layer,
      ChromeServiceLive.layer,
      PrintServiceLive.layer,
      EmailConfig.live,
      EmailServiceLive.layer,
      EmailSourceServiceLive.layer,
      S3Config.live,
      S3Live.layer
    )
