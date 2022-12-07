package ru.johnspade.nastenka.models

import zio.json.*

import java.util.UUID
import java.time.Instant

final case class Pin(
    id: UUID,
    createdAt: Instant,
    pinType: PinType,
    title: Option[String],
    text: Option[String],
    sender: Option[String],
    html: Option[String]
)

object Pin:
  given jsonCodec: JsonCodec[Pin] = DeriveJsonCodec.gen[Pin]

final case class NewPin(
    pinType: PinType,
    title: Option[String] = None,
    text: Option[String] = None,
    sender: Option[String] = None,
    html: Option[String] = None
)
