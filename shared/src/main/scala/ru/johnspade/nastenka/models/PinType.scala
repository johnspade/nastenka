package ru.johnspade.nastenka.models

import zio.json.*

enum PinType:
  case TELEGRAM_MESSAGE, EMAIL, BOOKMARK, FILE, TEXT

object PinType:
  given jsonEncoder: JsonEncoder[PinType] = JsonEncoder.string.contramap(_.toString())
  given jsonDecoder: JsonDecoder[PinType] = JsonDecoder.string.map(PinType.valueOf(_))
