package ru.johnspade.nastenka.inbox

import io.github.arainko.ducktape.*
import zio.*

import java.util.UUID
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.NewPin
import ru.johnspade.nastenka.models.Pin
import ru.johnspade.nastenka.persistence.InvestigationRepository

trait InboxService:
  def getInvestigations: ZIO[Any, Nothing, List[Investigation]]

  def addPin(investigationId: UUID, newPin: NewPin): ZIO[Any, Nothing, Unit]

class InboxServiceLive(investigationRepo: InvestigationRepository) extends InboxService:
  override def getInvestigations: ZIO[Any, Nothing, List[Investigation]] = investigationRepo.getAll.orDie

  override def addPin(investigationId: UUID, newPin: NewPin): ZIO[Any, Nothing, Unit] =
    (for
      clock <- ZIO.clock
      now   <- clock.instant
      id    <- ZIO.attempt(UUID.randomUUID())
      pin = newPin.into[Pin].transform(Field.const(_.createdAt, now), Field.const(_.id, id)) // todo extract method
      _ <- investigationRepo.addPin(investigationId, pin)
    yield ()).orDie

object InboxServiceLive:
  val layer = ZLayer.fromFunction(new InboxServiceLive(_))
