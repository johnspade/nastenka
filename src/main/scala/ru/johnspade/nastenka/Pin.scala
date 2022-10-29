package ru.johnspade.nastenka

enum Pin:
  case Bookmark(url: String, title: String)
  case TelegramMessage(from: String, text: String)
