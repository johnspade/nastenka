package ru.johnspade.nastenka.models

import zio.json.*

import java.time.Instant
import java.util.UUID

final case class Investigation(
    id: UUID,
    createdAt: Instant,
    title: String,
    pinsOrder: Seq[UUID],
    deleted: Boolean = false
)
object Investigation:
  given jsonCodec: JsonCodec[Investigation] = DeriveJsonCodec.gen

final case class InvestigationsResponse(investigations: List[Investigation])
object InvestigationsResponse:
  given jsonCodec: JsonCodec[InvestigationsResponse] = DeriveJsonCodec.gen

final case class InvestigationFull(id: UUID, createdAt: Instant, title: String, pins: Seq[Pin], pinsOrder: Seq[UUID])
object InvestigationFull:
  given jsonCodec: JsonCodec[InvestigationFull] = DeriveJsonCodec.gen

final case class NewInvestigation(title: String)
object NewInvestigation:
  given jsonCodec: JsonCodec[NewInvestigation] = DeriveJsonCodec.gen

final case class UpdatedInvestigation(id: UUID, title: String, pinsOrder: Seq[UUID])
object UpdatedInvestigation:
  given jsonCodec: JsonCodec[UpdatedInvestigation] = DeriveJsonCodec.gen
