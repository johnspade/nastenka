package ru.johnspade.nastenka.api

import zio.Config
import zio.config.*

import java.net.URI

import Config.*

final case class DbConfig(driver: String, url: String, user: String, password: String)

object DbConfig:
  val descriptor: Config[DbConfig] =
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
