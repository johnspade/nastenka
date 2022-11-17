package ru.johnspade.nastenka.email

import cats.syntax.all.*
import emil.Mail
import emil.SearchQuery.*
import emil.*
import emil.builder.*
import emil.javamail.*
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*

import java.util.UUID
import scala.util.Try
import jakarta.mail.internet.MimeMessage

trait EmailService:
  def collectEmailsToProcess(
      investigationIds: List[UUID],
      alreadyProcessed: List[String]
  ): Task[Vector[MailData]]

final class EmailServiceLive(emailConfig: EmailConfig) extends EmailService:

  private val emil    = JavaMailEmil[Task]()
  private val runEmil = emil(emailConfig.imapConfig)
  private val a       = emil.access

  override def collectEmailsToProcess(
      investigationIds: List[UUID],
      alreadyProcessed: List[String]
  ): Task[Vector[MailData]] =
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

    for
      folder    <- findNastenkaFolder
      allEmails <- folder.map(f => getAllEmails(f)).getOrElse(emptySearchResult)
      messageIdsToProcess = allEmails.mails
        .map { m =>
          m.messageId -> getInvestigationIds(m.recipients)
        }
        .filterNot { case (messageIdOpt, targetInvestigationIds) =>
          messageIdOpt.exists(id => alreadyProcessed.contains(id)) ||
            targetInvestigationIds.intersect(investigationIds).isEmpty
        }
        .toMap
        .collect { case (Some(messageId), targetInvestigationIds) =>
          messageId -> targetInvestigationIds
        }
      emailsToProcess <- folder
        .map { f =>
          loadEmails(messageIdsToProcess.keySet.toVector, f)
        }
        .getOrElse(emptySearchResult)
      result <- ZIO.collectAll(
        emailsToProcess.mails
          .collect {
            case Mail(
                  MailHeader(_, Some(messageId), _, _, _, _, _, _, subject, _, _),
                  _,
                  body,
                  attachments
                ) =>
              for
                htmlPart <- body.htmlPart
                html     <- ZIO.fromEither(htmlPart.get.contentDecode)
                investigationIds = messageIdsToProcess.get(messageId).getOrElse(List.empty)
                emlFileOpt       = attachments.all.find(_.mimeType.baseType == MimeType("message", "rfc822"))
              yield MailData(
                messageId = messageId,
                subject = subject,
                body = html,
                investigationIds = investigationIds
              )
          }
      )
    yield result
  end collectEmailsToProcess

  private def emptySearchResult[A] = ZIO.succeed(SearchResult[A](Vector.empty))

  def getInvestigationIds(recipients: Recipients) =
    recipients.to
      .map(_.address)
      .collect { case InvestigationId(id) =>
        id
      }

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
  val layer = ZLayer.fromFunction(new EmailServiceLive(_))
