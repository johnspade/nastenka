package ru.johnspade.nastenka.telegram

import ru.johnspade.nastenka.inbox.InboxService
import ru.johnspade.nastenka.models.NewPin
import ru.johnspade.nastenka.models.PinType
import telegramium.bots.ChatIntId
import telegramium.bots.Message
import telegramium.bots.client.Method
import telegramium.bots.high.Api
import telegramium.bots.high.WebhookBot
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*
import ru.johnspade.nastenka.telegram.TelegramBotApi.TelegramBotApi

final class TelegramBot(botConfig: BotConfig, inboxService: InboxService)(using api: Api[Task])
    extends WebhookBot[Task](api, url = s"${botConfig.url}/${botConfig.token}", path = botConfig.token):

  override def onMessageReply(msg: Message): Task[Option[Method[_]]] =
    ZIO.foreach(
      for
        _ <- msg.forwardDate if msg.from.exists(_.id == 354105900L)
        senderName = msg.forwardSenderName
          .orElse {
            msg.forwardFrom
              .map(u => u.firstName + u.lastName.map(" " + _).getOrElse(""))
          }
        text <- msg.text
      yield {
        for
          investigations <- inboxService.getInvestigations
          _ <- inboxService.addPin(
            investigations.head.id,
            NewPin(PinType.TELEGRAM_MESSAGE, text = Some(text), sender = senderName)
          )
        yield sendMessage(
          ChatIntId(msg.chat.id),
          "Added a new pin"
        )
      }
    )(identity)

end TelegramBot

object TelegramBot:
  val live = ZLayer(
    for
      api          <- ZIO.service[TelegramBotApi]
      botConfig    <- ZIO.service[BotConfig]
      inboxService <- ZIO.service[InboxService]
    yield new TelegramBot(botConfig, inboxService)(using api)
  )
