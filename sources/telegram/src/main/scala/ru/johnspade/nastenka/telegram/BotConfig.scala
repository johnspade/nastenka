package ru.johnspade.nastenka.telegram

import zio.config.*, ConfigDescriptor.*, ConfigSource.*

final case class BotConfig(token: String, userId: Long)

object BotConfig:
  val descriptor =
    (string("BOT_TOKEN") zip long("BOT_USER_ID")).to[BotConfig]

  val live = ZConfig.fromSystemEnv(descriptor).orDie
