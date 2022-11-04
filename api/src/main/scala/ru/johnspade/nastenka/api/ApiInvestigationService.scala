package ru.johnspade.nastenka.api

import zio.ZIO
import java.util.UUID
import io.github.arainko.ducktape.*
import zio.ZLayer
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.NewInvestigation
import ru.johnspade.nastenka.models.InvestigationFull

trait ApiInvestigationService:
  def getAll: ZIO[Any, Nothing, List[Investigation]]

  def getFull(id: UUID): ZIO[Any, Nothing, InvestigationFull]

  def create(newInvestigation: NewInvestigation): ZIO[Any, Nothing, Investigation]

  def save(investigation: Investigation): ZIO[Any, Nothing, Investigation]

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

object ApiInvestigationServiceLive:
  val layer = ZLayer.fromFunction(new ApiInvestigationServiceLive(_))
