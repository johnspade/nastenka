package ru.johnspade.nastenka.telegram

import zio.*

final case class BotConfig(token: String, userId: Long)

object BotConfig:
  val descriptor =
    (Config.string("BOT_TOKEN") ++ Config.bigInt("BOT_USER_ID")).map { case (token, userId) =>
      BotConfig(token, userId.toLong)
    }
