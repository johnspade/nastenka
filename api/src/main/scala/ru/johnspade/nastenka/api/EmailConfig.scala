package ru.johnspade.nastenka.api

import zio.config.*, ConfigDescriptor.*, ConfigSource.*

final case class EmailConfig(username: String, domain: String)

object EmailConfig:
  private val descriptor = string("EMAIL_NASTENKA_ALIAS").map { alias =>
    val parts = alias.split('@')
    EmailConfig(parts(0), parts(1))
  }

  val live = ZConfig.fromSystemEnv(descriptor).orDie
