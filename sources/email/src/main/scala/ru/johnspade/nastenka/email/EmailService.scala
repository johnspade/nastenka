package ru.johnspade.nastenka.email

import cats.syntax.all.*
import emil.SearchQuery.*
import emil.*
import emil.builder.*
import emil.javamail.*
import emil.javamail.conv.codec.given
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*

import java.util.UUID
import scala.util.Try

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

    def loadEmails(messageIds: Vector[String], folder: MailFolder) =
      if messageIds.isEmpty then emptySearchResult
      else
        runEmil.run {
          a.searchAndLoad(folder, 999)(Or(messageIds.map(SearchQuery.MessageID(_))))
        }

    def collectEmails(folder: MailFolder) =
      for
        allEmails <- runEmil.run(
          a.search(folder, 999)(All)
        )
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
        emailsToProcess <- loadEmails(messageIdsToProcess.keySet.toVector, folder)
        result <- ZIO.collectAll(
          emailsToProcess.mails
            .collect {
              case message @ Mail(
                    MailHeader(_, Some(messageId), _, _, _, _, _, _, _, _, _),
                    _,
                    _,
                    attachments
                  ) =>
                val emlFile = attachments.all
                  .find(_.mimeType.baseType == MimeType("message", "rfc822"))
                val mailToPin = ZIO
                  .foreach(emlFile) {
                    _.content.compile
                      .to(Array)
                      .flatMap(JavaMailEmil.mailFromByteArray[Task](_))
                  }
                  .map(_.getOrElse(message))
                for
                  mail     <- mailToPin
                  htmlPart <- mail.body.htmlPart
                  html     <- ZIO.fromEither(htmlPart.get.contentDecode)
                  investigationIds = messageIdsToProcess.get(messageId).getOrElse(List.empty)
                yield MailData(
                  messageId = messageId,
                  from = mail.header.from.map(_.displayString),
                  subject = mail.header.subject,
                  body = html,
                  investigationIds = investigationIds
                )
            }
        )
      yield result

    findNastenkaFolder.flatMap { folderOpt =>
      ZIO.foreach(folderOpt)(collectEmails).map(_.getOrElse(Vector.empty))
    }
  end collectEmailsToProcess

  private def emptySearchResult[A] = ZIO.succeed(SearchResult[A](Vector.empty))

  private def getInvestigationIds(recipients: Recipients) =
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
