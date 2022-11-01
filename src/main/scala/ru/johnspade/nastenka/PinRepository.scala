package ru.johnspade.nastenka

import io.getquill._
import io.getquill.jdbczio.Quill
import zio.*

import java.sql.SQLException

trait PinRepository:
  def save(pin: Pin): ZIO[Any, SQLException, Unit]

class PinRepositoryLive(quill: Quill.Postgres[CompositeNamingStrategy2[SnakeCase, PluralizedTableNames]])
    extends PinRepository:
  import quill._

  override def save(pin: Pin): ZIO[Any, SQLException, Unit] = run(query[Pin].insertValue(lift(pin))).as(())

object PinRepositoryLive:
  val layer = ZLayer.fromFunction(new PinRepositoryLive(_))
