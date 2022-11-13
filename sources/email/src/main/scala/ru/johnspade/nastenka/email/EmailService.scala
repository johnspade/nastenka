package ru.johnspade.nastenka.email

import cats.kernel.Monoid
import cats.syntax.all.*
import cats.syntax.all.*
import emil.SearchQuery.*
import emil.*
import emil.builder.*
import emil.javamail.*
import ru.johnspade.nastenka.inbox.InboxService
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*
import zio.stream.ZStream

import java.util.UUID
import scala.util.Try

import zio.stream.ZSink
import ru.johnspade.nastenka.models.NewPin
import ru.johnspade.nastenka.models.PinType

trait EmailService:
  def createStream: ZStream[Any, Throwable, Unit]

final class EmailServiceLive(
    emailConfig: EmailConfig,
    processedEmailRepo: ProcessedEmailRepository,
    inboxService: InboxService,
    printService: PrintService
) extends EmailService:

  override def createStream: ZStream[Any, Throwable, Unit] =
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
      .fromSchedule(Schedule.spaced(1.minute))
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
      .mapZIO { mail =>
        for
          htmlPart <- mail.body.htmlPart
          html     <- ZIO.fromEither(htmlPart.get.contentDecode)
          uuid     <- ZIO.attemptBlocking(UUID.randomUUID())
          fileLink = uuid.toString
          _ <- printService.print(html, s"$uuid.pdf")
          pin = NewPin(
            PinType.EMAIL,
            title = Some(mail.header.subject),
            original = Some(html)
          ) // todo preserve FROM, add link
          investigationIds = getInvestigationIds(mail.header.recipients)
          _                = println(investigationIds)
          _ <- ZIO.foreachDiscard(investigationIds) { investigationId =>
            inboxService.addPin(investigationId, pin)
          } // todo do not discard errors
          _ <- ZIO.foreachDiscard(mail.header.messageId) { messageId =>
            processedEmailRepo.create(ProcessedEmail(messageId, mail.header.recipients.to.map(_.address)))
          }
        yield ()
      }
  end createStream

  private def getInvestigationIds(recipients: Recipients) =
    recipients.to
      .map(_.address)
      .collect { case InvestigationId(id) =>
        Try(UUID.fromString(id)).toOption
      }
      .flatten

  private def emptySearchResult[A] = ZIO.succeed(SearchResult[A](Vector.empty))

  private object InvestigationId:
    def unapply(email: String): Option[String] =
      val nameAndDomain = emailConfig.nastenkaAlias.split('@')
      val nameAndId     = nameAndDomain.head.split('+')
      if nameAndId.tail.nonEmpty then Some(nameAndId.tail.head) else None

end EmailServiceLive

object EmailServiceLive:
  val layer = ZLayer.fromFunction(new EmailServiceLive(_, _, _, _))
