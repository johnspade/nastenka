package ru.johnspade.nastenka.inbox

import io.github.arainko.ducktape.*
import ru.johnspade.nastenka.errors.InvestigationNotFound
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.NewPin
import ru.johnspade.nastenka.models.Pin
import ru.johnspade.nastenka.persistence.InvestigationRepository
import zio.*
import zio.s3.*
import zio.stream.ZStream

import java.sql.SQLException
import java.util.UUID

trait InboxService:
  def getInvestigations: ZIO[Any, Nothing, List[Investigation]]

  def addPin(investigationId: UUID, newPin: NewPin): ZIO[Any, InvestigationNotFound | SQLException | Throwable, Unit]

  def saveFile(
      key: String,
      contentType: String,
      body: ZStream[Any, Throwable, Byte]
  ): ZIO[Any, Throwable, Unit]

final class InboxServiceLive(investigationRepo: InvestigationRepository, s3: S3, s3Config: S3Config)
    extends InboxService:

  override def getInvestigations: ZIO[Any, Nothing, List[Investigation]] = investigationRepo.getAll.orDie

  override def addPin(
      investigationId: UUID,
      newPin: NewPin
  ): ZIO[Any, InvestigationNotFound | SQLException | Throwable, Unit] =
    for
      clock <- ZIO.clock
      now   <- clock.instant
      id    <- ZIO.attempt(UUID.randomUUID())
      pin = newPin.into[Pin].transform(Field.const(_.createdAt, now), Field.const(_.id, id))
      _ <- investigationRepo.addPin(investigationId, pin)
    yield ()

  override def saveFile(
      key: String,
      contentType: String,
      body: ZStream[Any, Throwable, Byte]
  ): ZIO[Any, Throwable, Unit] =
    s3.multipartUpload(
      s3Config.bucketName,
      key,
      body,
      MultipartUploadOptions.fromUploadOptions(UploadOptions.fromContentType(contentType))
    )(4)

object InboxServiceLive:
  val layer = ZLayer.fromFunction(new InboxServiceLive(_, _, _))
