package ru.johnspade.nastenka.telegram

import cats.syntax.option.*
import ru.johnspade.nastenka.inbox.InboxService
import ru.johnspade.nastenka.models.NewPin
import ru.johnspade.nastenka.models.PinType
import ru.johnspade.nastenka.telegram.TelegramBotApi.TelegramBotApi
import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import telegramium.bots.CallbackQuery
import telegramium.bots.ChatIntId
import telegramium.bots.Message
import telegramium.bots.*
import telegramium.bots.high.Api
import telegramium.bots.high.LongPollBot
import telegramium.bots.high.implicits.*
import telegramium.bots.high.keyboards.*
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*

import java.util.UUID

final class TelegramBot(botConfig: BotConfig, inboxService: InboxService, sttpClient: SttpBackend[Task, ZioStreams])(
    using api: Api[Task]
) extends LongPollBot[Task](api):

  override def onMessage(msg: Message): Task[Unit] =
    if msg.forwardDate.isDefined && msg.from.exists(_.id == botConfig.userId) then
      inboxService.getInvestigations
        .flatMap { investigations =>
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
        .orElse(forwardedMsg.forwardFromChat.flatMap(_.title))
      text <- forwardedMsg.text.orElse(forwardedMsg.caption)
      messageEntities = forwardedMsg.text.map(_ => forwardedMsg.entities).getOrElse(forwardedMsg.captionEntities)
      html            = formatMessageWithEntities(text, messageEntities)
    yield {
      for
        savedPhotoOpt <- savePhotoIfPresent(forwardedMsg)
        _ <- inboxService.addPin(
          investigationId,
          NewPin(
            PinType.TelegramMessage,
            text = text.some,
            html = html.some,
            sender = senderName,
            images = savedPhotoOpt.toList
          )
        )
        _ <- answerCallbackQuery(query.id).exec
        _ <- sendMessage(ChatIntId(msg.chat.id), "Added a new pin").exec
      yield ()
    })
      .getOrElse(ZIO.unit)

  import scala.collection.mutable

  private def formatMessageWithEntities(message: String, entities: List[MessageEntity]): String = {
    val sortedEntities = entities.sortBy(e => (e.offset, -e.length))

    val insertions = mutable.Map[Int, (List[String], List[String])]().withDefaultValue((List.empty, List.empty))

    insertions(0) = (List.empty, List.empty)
    insertions(message.length()) = (List.empty, List.empty)

    sortedEntities.foreach { entity =>
      val start      = entity.offset
      val end        = start + entity.length
      val entityText = message.slice(start, end)

      val (tagStart, tagEnd) = entity match {
        case _: MentionMessageEntity          => ("", "")
        case _: CashtagMessageEntity          => ("", "")
        case _: CodeMessageEntity             => ("<code>", "</code>")
        case _: BotCommandMessageEntity       => ("", "")
        case _: CustomEmojiMessageEntity      => ("", "")
        case _: SpoilerMessageEntity          => ("<s>", "</s>")
        case _: EmailMessageEntity            => ("", "")
        case _: BoldMessageEntity             => ("<b>", "</b>")
        case _: PreMessageEntity              => ("<pre>", "</pre>")
        case _: ItalicMessageEntity           => ("<i>", "</i>")
        case _: StrikethroughMessageEntity    => ("<s>", "</s>")
        case _: UnderlineMessageEntity        => ("<u>", "</u>")
        case _: HashtagMessageEntity          => ("", "")
        case _: TextMentionMessageEntity      => ("", "")
        case TextLinkMessageEntity(_, _, url) => (s"""<a href="$url" target="_blank">""", "</a>")
        case _: UrlMessageEntity              => (s"""<a href="$entityText" target="_blank">""", "</a>")
        case _: PhoneNumberMessageEntity      => ("", "")
      }

      insertions(start) = (tagStart :: insertions(start)._1, insertions(start)._2)
      insertions(end) = (insertions(end)._1, tagEnd :: insertions(end)._2)
    }

    val sliceAts = insertions.keys.toList.sorted
    val textArr  = new mutable.ListBuffer[String]()
    textArr += insertions(sliceAts.head)._2.reverse.mkString + insertions(sliceAts.head)._1.reverse.mkString
    textArr ++= sliceAts.zip(sliceAts.tail).map { case (start, end) =>
      val (startTags, endTags) = insertions(end)
      val textSlice            = message.slice(start, end)
      textSlice + endTags.reverse.mkString + startTags.reverse.mkString
    }

    val formattedMessage = textArr.mkString.replace("\n", "<br>")

    s"""<div style="font-family: Segoe UI, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; font-size: 1rem; line-height: 1.6;">$formattedMessage</div>"""
  }

  private def savePhotoIfPresent(message: Message) =
    ZIO
      .foreach(message.photo.lastOption) { photo =>
        for
          randomUUID <- ZIO.randomWith(_.nextUUID)
          fileKey = randomUUID.toString() + ".jpg"
          file <- getFile(photo.fileId).exec
          _ <- basicRequest
            .get(uri"https://api.telegram.org/file/bot${botConfig.token}/${file.filePath}")
            .response(
              asStream(ZioStreams)(stream => inboxService.saveFile(fileKey, "image/jpeg", stream))
            )
            .send(sttpClient)
        yield fileKey
      }

end TelegramBot

object TelegramBot:
  val live = ZLayer(
    for
      api          <- ZIO.service[TelegramBotApi]
      botConfig    <- ZIO.config(BotConfig.descriptor)
      inboxService <- ZIO.service[InboxService]
      sttpClient   <- ZIO.service[SttpBackend[Task, ZioStreams]]
    yield new TelegramBot(botConfig, inboxService, sttpClient)(using api)
  )
