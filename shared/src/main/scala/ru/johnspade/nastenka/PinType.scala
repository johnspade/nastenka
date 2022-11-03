package ru.johnspade.nastenka

import zio.json.*

enum PinType:
  case TELEGRAM_MESSAGE, EMAIL, BOOKMARK, FILE, TEXT

object PinType:
  given jsonEncoder: JsonEncoder[PinType] = JsonEncoder.string.contramap(_.toString())
  given jsonDecoder: JsonDecoder[PinType] = JsonDecoder.string.map(PinType.valueOf(_))
// given quillEncoder: MappedEncoding[PinType, String] = MappedEncoding[PinType, String](_.toString())
// given quillDecoder: MappedEncoding[String, PinType] = MappedEncoding[String, PinType](s => PinType.valueOf(s))
