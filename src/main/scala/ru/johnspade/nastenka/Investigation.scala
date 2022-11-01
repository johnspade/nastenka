package ru.johnspade.nastenka

import zio.json.*

import java.util.UUID

final case class Investigation(id: UUID, title: String)
object Investigation:
  given encoder: JsonEncoder[Investigation] = DeriveJsonEncoder.gen[Investigation]

final case class InvestigationsResponse(investigations: List[Investigation])
object InvestigationsResponse:
  given encoder: JsonEncoder[InvestigationsResponse] = DeriveJsonEncoder.gen[InvestigationsResponse]

final case class InvestigationFull(id: UUID, title: String, pins: List[Pin])
object InvestigationFull:
  given encoder: JsonEncoder[InvestigationFull] = DeriveJsonEncoder.gen[InvestigationFull]
