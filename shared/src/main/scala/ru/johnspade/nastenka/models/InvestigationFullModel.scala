package ru.johnspade.nastenka.models

import zio.json.*

import java.time.Instant
import java.util.UUID

final case class InvestigationFullModel(
    id: UUID,
    title: String,
    pins: List[PinModel],
    pinsOrder: List[UUID],
    email: String
)
object InvestigationFullModel:
  given jsonCodec: JsonCodec[InvestigationFullModel] = DeriveJsonCodec.gen

final case class PinModel(
    id: UUID,
    pinType: PinType,
    title: Option[String],
    text: Option[String],
    sender: Option[String],
    original: Option[String]
)
object PinModel:
  given jsonCodec: JsonCodec[PinModel] = DeriveJsonCodec.gen
