package ru.johnspade.nastenka.persistence

import io.getquill.MappedEncoding
import io.getquill.SchemaMeta
import io.getquill.*
import io.getquill.context.jdbc.ArrayDecoders
import io.getquill.context.jdbc.ArrayEncoders
import ru.johnspade.nastenka.models.InvestigationPin
import ru.johnspade.nastenka.models.PinType

import java.util.UUID
import scala.collection.Factory

object codecs {
  inline given SchemaMeta[InvestigationPin] = schemaMeta[InvestigationPin]("investigations_pins")

  given MappedEncoding[PinType, String] = MappedEncoding[PinType, String](_.toString())
  given MappedEncoding[String, PinType] = MappedEncoding[String, PinType](s => PinType.valueOf(s))
}
