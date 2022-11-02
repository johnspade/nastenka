package ru.johnspade.nastenka

import zio.json.JsonEncoder
import zio.json.DeriveJsonEncoder
import java.util.UUID
import java.time.Instant

final case class Pin(
    id: UUID,
    createdAt: Instant,
    pinType: PinType,
    title: Option[String],
    text: Option[String],
    sender: Option[String],
    original: Option[String]
)

object Pin:
  given encoder: JsonEncoder[Pin] = DeriveJsonEncoder.gen[Pin]

final case class NewPin(
    pinType: PinType,
    title: Option[String],
    text: Option[String],
    sender: Option[String],
    original: Option[String]
)
