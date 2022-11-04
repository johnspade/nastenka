package ru.johnspade.nastenka.telegram

import zio.config.*, ConfigDescriptor.*, ConfigSource.*

final case class BotConfig(port: Int, url: String, token: String, userId: Long)

object BotConfig:
  val descriptor =
    (int("BOT_PORT") zip string("BOT_EXTERNAL_URL") zip string("BOT_TOKEN") zip long("BOT_USER_ID")).to[BotConfig]

  val live = ZConfig.fromSystemEnv(descriptor).orDie
