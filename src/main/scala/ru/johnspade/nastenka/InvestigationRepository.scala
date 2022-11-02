package ru.johnspade.nastenka

import io.getquill._
import io.getquill.jdbczio.Quill
import zio.*

import java.sql.SQLException
import java.util.UUID
import scala.collection.Factory

trait InvestigationRepository:
  def create(investigation: Investigation): ZIO[Any, SQLException, Investigation]

  def getAll: ZIO[Any, SQLException, List[Investigation]]

  def get(id: UUID): ZIO[Any, SQLException, Investigation]

  def getFull(id: UUID): ZIO[Any, SQLException, InvestigationFull]

  def update(investigation: Investigation): ZIO[Any, SQLException, Investigation]

  def addPin(investigation: Investigation, pin: Pin): ZIO[Any, Throwable, Unit]

class InvestigationRepositoryLive(quill: Quill.Postgres[CompositeNamingStrategy2[SnakeCase, PluralizedTableNames]])
    extends InvestigationRepository:
  import quill._

  private given arrayUuidDecoder[Col <: Seq[UUID]](using bf: Factory[UUID, Col]): Decoder[Col] =
    arrayRawDecoder[UUID, Col]
  private given arrayUuidEncoder[Col <: Seq[UUID]]: Encoder[Col] = arrayRawEncoder[UUID, Col]("uuid")

  private inline given SchemaMeta[InvestigationPin] = schemaMeta[InvestigationPin]("investigations_pins")

  override def create(investigation: Investigation): ZIO[Any, SQLException, Investigation] =
    run(query[Investigation].insertValue(lift(investigation))).as(investigation)

  override def getAll: ZIO[Any, SQLException, List[Investigation]] = run(query[Investigation])

  override def get(id: UUID): ZIO[Any, SQLException, Investigation] = run(
    query[Investigation].filter(_.id == lift(id))
  )
    .map(_.head)

  override def getFull(id: UUID): ZIO[Any, SQLException, InvestigationFull] =
    run(
      query[Investigation]
        .filter(_.id == lift(id))
        .join(query[InvestigationPin])
        .on { case (investigation, investigationPin) =>
          investigationPin.investigationId == investigation.id
        }
        .join(query[Pin])
        .on { case ((_, investigationPin), pin) =>
          pin.id == investigationPin.pinId
        }
    )
      .map {
        _.groupBy(_._1._1).map { case (investigation, investigationPinsList) =>
          InvestigationFull(
            investigation.id,
            investigation.createdAt,
            investigation.title,
            investigationPinsList.map(_._2),
            investigation.pinsOrder
          )
        }.head
      }

  override def update(investigation: Investigation): ZIO[Any, SQLException, Investigation] =
    run(query[Investigation].update(_.title -> lift(investigation.title), _.pinsOrder -> lift(investigation.pinsOrder)))
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
