package ru.johnspade.nastenka.errors

import java.util.UUID

final case class InvestigationNotFound(id: UUID) extends Throwable

final case class PinNotFound(id: UUID) extends Throwable
