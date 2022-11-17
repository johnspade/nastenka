package ru.johnspade.nastenka.email

import java.util.UUID

final case class ProcessedEmail(messageId: String, investigationIds: List[UUID])
