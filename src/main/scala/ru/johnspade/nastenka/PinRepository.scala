package ru.johnspade.nastenka

import io.getquill._
import io.getquill.jdbczio.Quill
import zio.*

import java.sql.SQLException

trait PinRepository:
  def create(pin: Pin): ZIO[Any, SQLException, Pin]

class PinRepositoryLive(quill: Quill.Postgres[CompositeNamingStrategy2[SnakeCase, PluralizedTableNames]])
    extends PinRepository:
  import quill._

  override def create(pin: Pin): ZIO[Any, SQLException, Pin] = run(query[Pin].insertValue(lift(pin))).as(pin)

object PinRepositoryLive:
  val layer = ZLayer.fromFunction(new PinRepositoryLive(_))
