package ru.johnspade.nastenka.email

import emil.Mail
import zio.Task
import java.util.UUID

final case class MailData(
    messageId: String,
    from: Option[String],
    subject: String,
    htmlBody: Option[String],
    textBody: Option[String],
    investigationIds: List[UUID]
)
