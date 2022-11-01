package ru.johnspade.nastenka

import zio.json.JsonEncoder
import zio.json.DeriveJsonEncoder
import java.util.UUID

final case class Pin(
    id: UUID,
    pinType: PinType,
    title: Option[String],
    text: Option[String],
    sender: Option[String],
    original: Option[String]
)

object Pin:
  given encoder: JsonEncoder[Pin] = DeriveJsonEncoder.gen[Pin]
