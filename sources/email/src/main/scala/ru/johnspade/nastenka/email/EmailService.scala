package ru.johnspade.nastenka.email

import cats.kernel.Monoid
import cats.syntax.all.*
import cats.syntax.all.*
import emil.SearchQuery.*
import emil.*
import emil.builder.*
import emil.javamail.*
import ru.johnspade.nastenka.inbox.InboxService
import ru.johnspade.nastenka.models.NewPin
import ru.johnspade.nastenka.models.PinType
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*
import zio.stream.ZSink
import zio.stream.ZStream

import java.util.UUID
import scala.util.Try

trait EmailService:
  def createStream: ZStream[Any, Throwable, Any]

final class EmailServiceLive(
    emailConfig: EmailConfig,
    processedEmailRepo: ProcessedEmailRepository,
    inboxService: InboxService,
    printService: PrintService
) extends EmailService:

  override def createStream: ZStream[Any, Throwable, Any] =
    val emil    = JavaMailEmil[Task]()
    val runEmil = emil(emailConfig.imapConfig)
    val a       = emil.access
    val findNastenkaFolder = runEmil.run {
      a.getInbox
        .flatMap(inbox => a.findFolder(Some(inbox), emailConfig.nastenkaFolder))
    }
    def getAllEmails(folder: MailFolder) =
      runEmil.run(
        a.search(folder, 999)(All)
      )

    def loadEmails(messageIds: Vector[String], folder: MailFolder) =
      val q = Or(messageIds.map(SearchQuery.MessageID(_)))
      runEmil.run {
        a.searchAndLoad(folder, 999)(q)
      }

    ZStream
      .fromSchedule(Schedule.once andThen Schedule.spaced(1.minute))
      .mapZIO { _ =>
        for
          processed <- processedEmailRepo.getAll
          processedIds = processed.map(_.messageId)
          investigations <- inboxService.getInvestigations
          investigationIds = investigations.map(_.id)
          folder    <- findNastenkaFolder
          allEmails <- folder.map(f => getAllEmails(f)).getOrElse(emptySearchResult)
          msgIdsToProcess = allEmails.mails
            .filterNot { m =>
              val investigationIds = getInvestigationIds(m.recipients)
              m.messageId.exists(id => processedIds.contains(id)) || investigationIds.isEmpty
            }
            .flatMap(_.messageId)
          emailsToProcess <- folder.map(f => loadEmails(msgIdsToProcess, f)).getOrElse(emptySearchResult)
        yield emailsToProcess.mails
      }
      .flattenIterables
      .schedule(Schedule.once andThen Schedule.spaced(5.seconds))
      .tap { mail =>
        for
          htmlPart  <- mail.body.htmlPart
          html      <- ZIO.fromEither(htmlPart.get.contentDecode)
          uuid      <- ZIO.attempt(UUID.randomUUID())
          pdfStream <- printService.print(html)
          _         <- inboxService.saveFile(key = s"$uuid.pdf", contentType = "application/pdf", body = pdfStream)
          pin = NewPin(
            PinType.EMAIL,
            title = Some(mail.header.subject),
            fileKey = Some(uuid),
            original = Some(html)
          ) // todo preserve FROM, add link
          investigationIds = getInvestigationIds(mail.header.recipients)
          _ <- ZIO.foreachDiscard(investigationIds) { investigationId =>
            inboxService.addPin(investigationId, pin)
          }
          _ <- ZIO.foreachDiscard(mail.header.messageId) { messageId =>
            processedEmailRepo
              .create(ProcessedEmail(messageId, mail.header.recipients.to.map(_.address)))
          }
        yield ()
      }
  end createStream

  private def getInvestigationIds(recipients: Recipients) =
    recipients.to
      .map(_.address)
      .collect { case InvestigationId(id) =>
        id
      }

  private def emptySearchResult[A] = ZIO.succeed(SearchResult[A](Vector.empty))

  private object InvestigationId:
    def unapply(email: String): Option[UUID] =
      val nameAndDomain = emailConfig.nastenkaAlias.split('@')
      val Name          = nameAndDomain.head
      val Domain        = nameAndDomain(1)
      email match
        case s"$Name+$id@$Domain" => Try(UUID.fromString(id)).toOption
        case _                    => None

end EmailServiceLive

object EmailServiceLive:
  val layer = ZLayer.fromFunction(new EmailServiceLive(_, _, _, _))
