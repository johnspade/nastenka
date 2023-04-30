package ru.johnspade.nastenka.api

import io.github.arainko.ducktape.*
import ru.johnspade.nastenka.errors.InvestigationNotFound
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.InvestigationFullModel
import ru.johnspade.nastenka.models.NewInvestigation
import ru.johnspade.nastenka.models.PinModel
import ru.johnspade.nastenka.models.UpdatedInvestigation
import zio.*

import java.util.UUID

trait ApiInvestigationService:
  def getAll: ZIO[Any, Nothing, List[Investigation]]

  def getFull(id: UUID): ZIO[Any, InvestigationNotFound, InvestigationFullModel]

  def create(newInvestigation: NewInvestigation): ZIO[Any, Nothing, Investigation]

  def save(id: UUID, investigation: UpdatedInvestigation): ZIO[Any, InvestigationNotFound, InvestigationFullModel]

  def getPin(pinId: UUID): ZIO[Any, Nothing, PinModel]

  def delete(id: UUID): ZIO[Any, Nothing, Unit]

class ApiInvestigationServiceLive(investigationRepo: ApiInvestigationRepository, emailConfig: EmailConfig)
    extends ApiInvestigationService:
  override def getAll: ZIO[Any, Nothing, List[Investigation]] =
    investigationRepo.getAll.orDie

  override def getFull(id: UUID): ZIO[Any, InvestigationNotFound, InvestigationFullModel] =
    import emailConfig._

    investigationRepo
      .getFull(id)
      .refineToOrDie[InvestigationNotFound]
      .map(
        _.into[InvestigationFullModel]
          .transform(Field.const(_.email, s"$username+$id@$domain"))
      )

  override def create(newInvestigation: NewInvestigation): ZIO[Any, Nothing, Investigation] =
    (for
      clock <- ZIO.clock
      now   <- clock.instant
      id    <- ZIO.attempt(UUID.randomUUID)
      investigation = Investigation(id, now, newInvestigation.title, List.empty)
      savedInvestigation <- investigationRepo.create(investigation)
    yield savedInvestigation).orDie

  // todo validate pins order
  override def save(
      id: UUID,
      investigation: UpdatedInvestigation
  ): ZIO[Any, InvestigationNotFound, InvestigationFullModel] =
    investigationRepo
      .update(id, investigation)
      .orDie *> getFull(id)

  override def getPin(pinId: UUID): ZIO[Any, Nothing, PinModel] =
    investigationRepo.getPin(pinId).orDie.map(_.to[PinModel])

  override def delete(id: UUID): ZIO[Any, Nothing, Unit] = investigationRepo.delete(id).orDie

object ApiInvestigationServiceLive:
  val layer = ZLayer(
    for
      investigationRepo <- ZIO.service[ApiInvestigationRepository]
      emailConfig       <- ZIO.config(EmailConfig.descriptor)
    yield new ApiInvestigationServiceLive(investigationRepo, emailConfig)
  )
