package ru.johnspade.nastenka.api

import io.github.arainko.ducktape.*
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.InvestigationFullModel
import ru.johnspade.nastenka.models.NewInvestigation
import ru.johnspade.nastenka.models.PinModel
import ru.johnspade.nastenka.models.UpdatedInvestigation
import zio.*

import java.util.UUID

trait ApiInvestigationService:
  def getAll: ZIO[Any, Nothing, List[Investigation]]

  def getFull(id: UUID): ZIO[Any, Nothing, InvestigationFullModel]

  def create(newInvestigation: NewInvestigation): ZIO[Any, Nothing, Investigation]

  def save(id: UUID, investigation: UpdatedInvestigation): ZIO[Any, Nothing, InvestigationFullModel]

  def getPin(pinId: UUID): ZIO[Any, Nothing, PinModel]

class ApiInvestigationServiceLive(investigationRepo: ApiInvestigationRepository, emailConfig: EmailConfig)
    extends ApiInvestigationService:
  override def getAll: ZIO[Any, Nothing, List[Investigation]] =
    investigationRepo.getAll.orDie

  override def getFull(id: UUID): ZIO[Any, Nothing, InvestigationFullModel] =
    import emailConfig._

    investigationRepo
      .getFull(id)
      .orDie
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
  override def save(id: UUID, investigation: UpdatedInvestigation): ZIO[Any, Nothing, InvestigationFullModel] =
    investigationRepo
      .update(id, investigation)
      .orDie *> getFull(id)

  override def getPin(pinId: UUID): ZIO[Any, Nothing, PinModel] =
    investigationRepo.getPin(pinId).orDie.map(_.to[PinModel])

object ApiInvestigationServiceLive:
  val layer = ZLayer.fromFunction(new ApiInvestigationServiceLive(_, _))
