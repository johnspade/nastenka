package ru.johnspade.nastenka.api

import org.flywaydb.core.Flyway
import zio.*

object FlywayMigration:
  val migrate: ZIO[Any, Throwable, Unit] =
    ZIO.config(DbConfig.descriptor).flatMap { cfg =>
      ZIO.attemptBlocking {
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .baselineOnMigrate(true)
          .load()
          .migrate()
      }.unit
    }
