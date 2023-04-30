package ru.johnspade.nastenka.api

import zio.Config

import Config.*

final case class EmailConfig(username: String, domain: String)

object EmailConfig:
  val descriptor = string("EMAIL_NASTENKA_ALIAS").map { alias =>
    val parts = alias.split('@')
    EmailConfig(parts(0), parts(1))
  }
