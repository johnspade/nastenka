package ru.johnspade.nastenka.telegram

import cats.syntax.option.*
import ru.johnspade.nastenka.inbox.InboxService
import ru.johnspade.nastenka.models.NewPin
import ru.johnspade.nastenka.models.PinType
import ru.johnspade.nastenka.telegram.TelegramBotApi.TelegramBotApi
import telegramium.bots.CallbackQuery
import telegramium.bots.ChatIntId
import telegramium.bots.Message
import telegramium.bots.high.Api
import telegramium.bots.high.LongPollBot
import telegramium.bots.high.implicits.*
import telegramium.bots.high.keyboards.*
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*

import java.util.UUID

final class TelegramBot(botConfig: BotConfig, inboxService: InboxService)(using api: Api[Task])
    extends LongPollBot[Task](api):

  override def onMessage(msg: Message): Task[Unit] =
    if msg.forwardDate.isDefined && msg.from.exists(_.id == botConfig.userId) then
      inboxService.getInvestigations
        .map { investigations =>
          sendMessage(
            ChatIntId(msg.chat.id),
            "Select an investigation",
            replyToMessageId = msg.messageId.some,
            replyMarkup = InlineKeyboardMarkups
              .singleColumn(
                investigations
                  .take(10)
                  .map(investigation =>
                    InlineKeyboardButtons.callbackData(investigation.title, investigation.id.toString())
                  )
              )
              .some
          ).exec.unit
        }
    else ZIO.unit

  override def onCallbackQuery(query: CallbackQuery): Task[Unit] =
    (for
      callbackData <- query.data if query.from.id == botConfig.userId
      investigationId = UUID.fromString(callbackData)
      msg          <- query.message
      forwardedMsg <- msg.replyToMessage
      senderName = forwardedMsg.forwardSenderName
        .orElse {
          forwardedMsg.forwardFrom
            .map(u => u.firstName + u.lastName.map(" " + _).getOrElse(""))
        }
      text <- forwardedMsg.text
    yield {
      for
        _ <- inboxService.addPin(
          investigationId,
          NewPin(PinType.TELEGRAM_MESSAGE, text = text.some, sender = senderName)
        )
        _ <- answerCallbackQuery(query.id).exec
        _ <- sendMessage(ChatIntId(msg.chat.id), "Added a new pin").exec
      yield ()
    })
      .getOrElse(ZIO.unit)

end TelegramBot

object TelegramBot:
  val live = ZLayer(
    for
      api          <- ZIO.service[TelegramBotApi]
      botConfig    <- ZIO.service[BotConfig]
      inboxService <- ZIO.service[InboxService]
    yield new TelegramBot(botConfig, inboxService)(using api)
  )
