package ru.johnspade.nastenka.telegram

import zio.config.*, ConfigDescriptor.*, ConfigSource.*

final case class BotConfig(port: Int, url: String, token: String)

object BotConfig:
  val descriptor = (int("BOT_PORT") zip string("BOT_EXTERNAL_URL") zip string("BOT_TOKEN")).to[BotConfig]

  val live = ZConfig.fromSystemEnv(descriptor).orDie
