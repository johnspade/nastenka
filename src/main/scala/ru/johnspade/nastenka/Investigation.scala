package ru.johnspade.nastenka

import zio.json.*

import java.util.UUID
import java.time.Instant

final case class Investigation(id: UUID, createdAt: Instant, title: String, pinsOrder: List[UUID])
object Investigation:
  given jsonCodec: JsonCodec[Investigation] = DeriveJsonCodec.gen

final case class InvestigationsResponse(investigations: List[Investigation])
object InvestigationsResponse:
  given jsonCodec: JsonCodec[InvestigationsResponse] = DeriveJsonCodec.gen

final case class InvestigationFull(id: UUID, createdAt: Instant, title: String, pins: List[Pin], pinsOrder: List[UUID])
object InvestigationFull:
  given encoder: JsonEncoder[InvestigationFull] = DeriveJsonEncoder.gen[InvestigationFull]

final case class NewInvestigation(title: String)
object NewInvestigation:
  given jsonCodec: JsonCodec[NewInvestigation] = DeriveJsonCodec.gen
