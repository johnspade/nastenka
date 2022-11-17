package ru.johnspade.nastenka.email

import emil.Mail
import zio.Task
import java.util.UUID

final case class MailData(
    messageId: String,
    subject: String,
    body: String,
    investigationIds: List[UUID]
)
