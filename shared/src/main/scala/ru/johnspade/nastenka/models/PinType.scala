package ru.johnspade.nastenka.models

import zio.json.*

object PinType:
  val TelegramMessage = "TELEGRAM_MESSAGE"
  val Email           = "EMAIL"
  val Bookmark        = "BOOKMARK"
  val File            = "FILE"
  val Text            = "TEXT"
