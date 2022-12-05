package ru.johnspade.nastenka.api

import io.github.arainko.ducktape.*
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.InvestigationFull
import ru.johnspade.nastenka.models.NewInvestigation
import ru.johnspade.nastenka.models.PinModel
import zio.ZIO
import zio.ZLayer

import java.util.UUID

trait ApiInvestigationService:
  def getAll: ZIO[Any, Nothing, List[Investigation]]

  def getFull(id: UUID): ZIO[Any, Nothing, InvestigationFull]

  def create(newInvestigation: NewInvestigation): ZIO[Any, Nothing, Investigation]

  def save(investigation: Investigation): ZIO[Any, Nothing, Investigation]

  def getPin(pinId: UUID): ZIO[Any, Nothing, PinModel]

class ApiInvestigationServiceLive(investigationRepo: ApiInvestigationRepository) extends ApiInvestigationService:
  override def getAll: ZIO[Any, Nothing, List[Investigation]] =
    investigationRepo.getAll.orDie

  override def getFull(id: UUID): ZIO[Any, Nothing, InvestigationFull] =
    investigationRepo.getFull(id).orDie

  override def create(newInvestigation: NewInvestigation): ZIO[Any, Nothing, Investigation] =
    (for
      clock <- ZIO.clock
      now   <- clock.instant
      id    <- ZIO.attempt(UUID.randomUUID)
      investigation = Investigation(id, now, newInvestigation.title, List.empty)
      savedInvestigation <- investigationRepo.create(investigation)
    yield savedInvestigation).orDie

  // todo validate pins order
  override def save(investigation: Investigation): ZIO[Any, Nothing, Investigation] =
    investigationRepo.update(investigation).orDie

  override def getPin(pinId: UUID): ZIO[Any, Nothing, PinModel] =
    investigationRepo.getPin(pinId).orDie.map(_.to[PinModel])

object ApiInvestigationServiceLive:
  val layer = ZLayer.fromFunction(new ApiInvestigationServiceLive(_))
