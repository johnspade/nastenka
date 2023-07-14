package ru.johnspade.nastenka.models

import zio.json.*

import java.time.Instant
import java.util.UUID

final case class InvestigationFullModel(
    id: UUID,
    title: String,
    pins: Seq[PinModel],
    pinsOrder: Seq[UUID],
    email: String
)
object InvestigationFullModel:
  given jsonCodec: JsonCodec[InvestigationFullModel] = DeriveJsonCodec.gen

final case class PinModel(
    id: UUID,
    pinType: String,
    title: Option[String],
    url: Option[String],
    text: Option[String],
    sender: Option[String],
    html: Option[String],
    images: List[String]
)
object PinModel:
  given jsonCodec: JsonCodec[PinModel] = DeriveJsonCodec.gen
