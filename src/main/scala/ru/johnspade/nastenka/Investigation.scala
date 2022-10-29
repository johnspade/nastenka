package ru.johnspade.nastenka

import zio.json.*

import java.util.UUID

final case class Investigation(id: UUID, title: String, pins: List[Pin])

final case class InvestigationItem(id: UUID, title: String)
object InvestigationItem:
  given encoder: JsonEncoder[InvestigationItem] = DeriveJsonEncoder.gen[InvestigationItem]

final case class InvestigationList(investigations: List[InvestigationItem])
object InvestigationList:
  given encoder: JsonEncoder[InvestigationList] = DeriveJsonEncoder.gen[InvestigationList]
