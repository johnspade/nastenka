package ru.johnspade.nastenka.email

final case class ProcessedEmail(messageId: String, recipients: List[String])
