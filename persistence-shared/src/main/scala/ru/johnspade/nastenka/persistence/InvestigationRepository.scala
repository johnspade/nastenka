package ru.johnspade.nastenka.persistence

import io.getquill.*
import io.getquill.jdbczio.Quill
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.InvestigationPin
import ru.johnspade.nastenka.models.Pin
import ru.johnspade.nastenka.models.PinType
import zio.*

import java.sql.SQLException
import java.util.UUID
import scala.collection.Factory

import codecs.given

trait InvestigationRepository:
  def getAll: ZIO[Any, SQLException, List[Investigation]]

  def get(id: UUID): ZIO[Any, SQLException, Investigation]

  def update(investigation: Investigation): ZIO[Any, SQLException, Investigation]

  def addPin(investigation: Investigation, pin: Pin): ZIO[Any, Throwable, Unit]

class InvestigationRepositoryLive(quill: Quill.Postgres[CompositeNamingStrategy2[SnakeCase, PluralizedTableNames]])
    extends InvestigationRepository:
  import quill.*

  private given arrayUuidDecoder[Col <: Seq[UUID]](using bf: Factory[UUID, Col]): Decoder[Col] =
    arrayRawDecoder[UUID, Col]
  private given arrayUuidEncoder[Col <: Seq[UUID]]: Encoder[Col] = arrayRawEncoder[UUID, Col]("uuid")

  override def getAll: ZIO[Any, SQLException, List[Investigation]] = run(query[Investigation])

  override def get(id: UUID): ZIO[Any, SQLException, Investigation] = run(
    query[Investigation].filter(_.id == lift(id))
  )
    .map(_.head)

  override def update(investigation: Investigation): ZIO[Any, SQLException, Investigation] =
    run(
      query[Investigation]
        .filter(_.id == lift(investigation.id))
        .update(_.title -> lift(investigation.title), _.pinsOrder -> lift(investigation.pinsOrder))
    )
      .as(investigation)

  override def addPin(investigation: Investigation, pin: Pin): ZIO[Any, Throwable, Unit] =
    val savePin = run(query[Pin].insertValue(lift(pin)))
    val saveInvestigationPin = run(
      query[InvestigationPin].insertValue(lift(InvestigationPin(investigation.id, pin.id)))
    )
    val saveInvestigation = update(investigation)
    transaction(savePin *> saveInvestigationPin *> saveInvestigation).as(())

object InvestigationRepositoryLive:
  val layer = ZLayer.fromFunction(new InvestigationRepositoryLive(_))
