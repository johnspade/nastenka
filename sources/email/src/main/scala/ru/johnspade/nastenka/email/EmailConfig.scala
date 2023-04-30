package ru.johnspade.nastenka.email

import emil.MailConfig
import emil.SSLType
import zio.Config

import Config.*

case class EmailConfig(url: String, user: String, password: String, nastenkaAlias: String, nastenkaFolder: String):
  val imapConfig = MailConfig(url, user, password, SSLType.SSL)

object EmailConfig:
  val descriptor =
    (string("EMAIL_URL") zip
      string("EMAIL_USER") zip
      string("EMAIL_PASSWORD") zip
      string("EMAIL_NASTENKA_ALIAS") zip
      string("EMAIL_FOLDER"))
      .map { case (url, user, password, nastenkaAlias, nastenkaFolder) =>
        EmailConfig(url, user, password, nastenkaAlias, nastenkaFolder)
      }
