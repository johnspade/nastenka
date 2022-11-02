package ru.johnspade.nastenka

import io.github.arainko.ducktape.*
import zio.*

import java.util.UUID

trait PublicInvestigationService:
  def getInvestigations: ZIO[Any, Nothing, List[Investigation]]

  def addPin(investigationId: UUID, newPin: NewPin): ZIO[Any, Nothing, Unit]

class PublicInvestigationServiceLive(investigationRepo: InvestigationRepository) extends PublicInvestigationService:
  override def getInvestigations: ZIO[Any, Nothing, List[Investigation]] = investigationRepo.getAll.orDie

  override def addPin(investigationId: UUID, newPin: NewPin): ZIO[Any, Nothing, Unit] =
    (for
      clock <- ZIO.clock
      now   <- clock.instant
      id    <- ZIO.attempt(UUID.randomUUID())
      pin = newPin.into[Pin].transform(Field.const(_.createdAt, now), Field.const(_.id, id)) // todo extract method
      investigation <- investigationRepo.get(investigationId)
      updatedInvestigation = investigation.copy(pinsOrder = investigation.pinsOrder :+ id)
      _ <- investigationRepo.addPin(investigation, pin)
    yield ()).orDie

object PublicInvestigationServiceLive:
  val layer = ZLayer.fromFunction(new PublicInvestigationServiceLive(_))
