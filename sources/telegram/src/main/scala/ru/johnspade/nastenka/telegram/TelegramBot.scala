package ru.johnspade.nastenka.telegram

import telegramium.bots.Message
import telegramium.bots.client.Method
import telegramium.bots.high.Api
import telegramium.bots.high.WebhookBot
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*

final class TelegramBot(botConfig: BotConfig)(using api: Api[Task])
    extends WebhookBot[Task](api, url = s"${botConfig.url}/${botConfig.token}", path = botConfig.token):
  override def onMessageReply(msg: Message): Task[Option[Method[_]]] = ???
