package ru.johnspade.nastenka

import io.getquill._
import io.getquill.jdbczio.Quill
import zio.*

import java.sql.SQLException

trait InvestigationRepository:
  def getInvestigations: ZIO[Any, SQLException, List[InvestigationItem]]

class InvestigationRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends InvestigationRepository:
  import quill._

  val investigations = quote {
    querySchema[InvestigationItem]("investigations")
  }

  override def getInvestigations: ZIO[Any, SQLException, List[InvestigationItem]] = run(investigations)

object InvestigationRepositoryLive:
  val layer = ZLayer.fromFunction(new InvestigationRepositoryLive(_))
