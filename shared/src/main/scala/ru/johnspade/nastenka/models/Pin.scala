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
    fileKey: Option[UUID],
    original: Option[String]
)

object Pin:
  given jsonCodec: JsonCodec[Pin] = DeriveJsonCodec.gen[Pin]

final case class NewPin(
    pinType: PinType,
    title: Option[String] = None,
    text: Option[String] = None,
    sender: Option[String] = None,
    fileKey: Option[UUID] = None,
    original: Option[String] = None
)
