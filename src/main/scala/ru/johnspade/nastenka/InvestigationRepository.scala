package ru.johnspade.nastenka

import io.getquill._
import io.getquill.jdbczio.Quill
import zio.*

import java.sql.SQLException
import java.util.UUID

trait InvestigationRepository:
  def getAll: ZIO[Any, SQLException, List[Investigation]]

  def getFull(id: UUID): ZIO[Any, SQLException, InvestigationFull]

class InvestigationRepositoryLive(quill: Quill.Postgres[CompositeNamingStrategy2[SnakeCase, PluralizedTableNames]])
    extends InvestigationRepository:
  import quill._

  override def getAll: ZIO[Any, SQLException, List[Investigation]] = run(query[Investigation])

  private given SchemaMeta[InvestigationPin] = schemaMeta[InvestigationPin]("investigations_pins")

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
          InvestigationFull(investigation.id, investigation.title, investigationPinsList.map(_._2))
        }.head
      }

object InvestigationRepositoryLive:
  val layer = ZLayer.fromFunction(new InvestigationRepositoryLive(_))
