package ru.johnspade.nastenka.models

import zio.json.*

import java.util.UUID
import java.time.Instant

final case class Pin(
    id: UUID,
    createdAt: Instant,
    pinType: String,
    title: Option[String],
    url: Option[String],
    text: Option[String],
    sender: Option[String],
    html: Option[String],
    images: List[String],
    deleted: Boolean = false
)

object Pin:
  given jsonCodec: JsonCodec[Pin] = DeriveJsonCodec.gen[Pin]

final case class NewPin(
    pinType: String,
    title: Option[String] = None,
    url: Option[String] = None,
    text: Option[String] = None,
    sender: Option[String] = None,
    html: Option[String] = None,
    images: List[String] = List.empty
)
