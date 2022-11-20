package ru.johnspade.nastenka.email

import ru.johnspade.nastenka.errors.InvestigationNotFound
import ru.johnspade.nastenka.inbox.InboxService
import ru.johnspade.nastenka.models.NewPin
import ru.johnspade.nastenka.models.PinType
import zio.*
import zio.stream.ZSink
import zio.stream.ZStream

import java.util.UUID
import scala.util.Try

trait EmailSourceService:
  def createStream: ZStream[Any, Throwable, Any]

final class EmailSourceServiceLive(
    emailService: EmailService,
    processedEmailRepo: ProcessedEmailRepository,
    inboxService: InboxService,
    printService: PrintService
) extends EmailSourceService:

  override def createStream: ZStream[Any, Throwable, Any] =
    ZStream
      .fromSchedule(Schedule.once andThen Schedule.spaced(1.minute))
      .mapZIO { _ =>
        inboxService.getInvestigations.map(_.map(_.id))
      }
      .filterNot(_.isEmpty)
      .mapZIO { investigationIds =>
        for
          processed <- processedEmailRepo.getAll
          processedIds = processed.map(_.messageId)
          mails <- emailService.collectEmailsToProcess(investigationIds, processedIds)
        yield mails
      }
      .flattenIterables
      .schedule(Schedule.once andThen Schedule.spaced(5.seconds))
      .tap { mailData =>
        import mailData.{messageId, from, subject, body, investigationIds}

        (for
          uuid      <- ZIO.attempt(UUID.randomUUID())
          pdfStream <- printService.print(body)
          _ <- inboxService
            .saveFile(key = s"$uuid.pdf", contentType = "application/pdf", body = pdfStream)
            .retryN(1)
          pin = NewPin(
            PinType.EMAIL,
            sender = from,
            title = Some(subject),
            fileKey = Some(uuid)
          )
          _ <- ZIO
            .foreachDiscard(investigationIds) { investigationId =>
              inboxService.addPin(investigationId, pin)
            }
            .retryN(1)
            .catchAll(e => ZIO.logError(s"Cannot create pin for messageId=$messageId: $e"))
          _ <- processedEmailRepo
            .create(ProcessedEmail(messageId, investigationIds))
            .retryN(1)
        yield ())
          .retryN(1)
      }
      .onError(cause => ZIO.logErrorCause(s"EmailSource stream failed, retrying}", cause))
      .retry(Schedule.exponential(1.second))
  end createStream

end EmailSourceServiceLive

object EmailSourceServiceLive:
  val layer = ZLayer.fromFunction(new EmailSourceServiceLive(_, _, _, _))
