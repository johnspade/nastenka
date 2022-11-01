package ru.johnspade.nastenka

import zio.json.JsonEncoder
import io.getquill.MappedEncoding

enum PinType:
  case TELEGRAM_MESSAGE, EMAIL, BOOKMARK, FILE, TEXT

object PinType:
  given jsonEncoder: JsonEncoder[PinType]             = JsonEncoder.string.contramap(_.toString())
  given quillEncoder: MappedEncoding[PinType, String] = MappedEncoding[PinType, String](_.toString())
  given quillDecoder: MappedEncoding[String, PinType] = MappedEncoding[String, PinType](s => PinType.valueOf(s))
