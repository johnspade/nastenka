package ru.johnspade.nastenka

import java.net.URI

import zio.IO
import zio.config.*, ConfigDescriptor.*, ConfigSource.*

final case class DbConfig(driver: String, url: String, user: String, password: String)

object DbConfig:
  val dbConfig: ConfigDescriptor[DbConfig] =
    string("DATABASE_URL")
      .map { url =>
        val dbUri    = new URI(url)
        val userInfo = dbUri.getUserInfo.split(":")
        DbConfig(
          "org.postgresql.Driver",
          s"jdbc:postgresql://${dbUri.getHost}:${dbUri.getPort}${dbUri.getPath}",
          user = userInfo(0),
          password = userInfo(1)
        )
      }

  val live = ZConfig.fromSystemEnv(dbConfig).orDie
