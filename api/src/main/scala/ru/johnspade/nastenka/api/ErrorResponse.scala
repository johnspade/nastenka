package ru.johnspade.nastenka.api

import zio.json.JsonCodec
import zio.json.DeriveJsonCodec

/** RFC 7807 Problem Details for HTTP APIs
  */
final case class Problem(`type`: String, title: String, detail: Option[String] = None)

object Problem:
  given jsonCodec: JsonCodec[Problem] = DeriveJsonCodec.gen
