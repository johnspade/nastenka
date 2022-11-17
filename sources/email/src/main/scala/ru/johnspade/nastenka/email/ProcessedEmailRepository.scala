package ru.johnspade.nastenka.email

import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import java.sql.SQLException
import java.util.UUID
import scala.collection.Factory

trait ProcessedEmailRepository:
  def getAll: ZIO[Any, SQLException, List[ProcessedEmail]]

  def create(processed: ProcessedEmail): ZIO[Any, SQLException, Unit]

final class ProcessedEmailRepositoryLive(
    quill: Quill.Postgres[CompositeNamingStrategy2[SnakeCase, PluralizedTableNames]]
) extends ProcessedEmailRepository:
  import quill.*

  private given arrayUuidDecoder[Col <: Seq[UUID]](using bf: Factory[UUID, Col]): Decoder[Col] =
    arrayRawDecoder[UUID, Col]
  private given arrayUuidEncoder[Col <: Seq[UUID]]: Encoder[Col] = arrayRawEncoder[UUID, Col]("uuid")

  override def getAll: ZIO[Any, SQLException, List[ProcessedEmail]] =
    run(query[ProcessedEmail])

  override def create(processed: ProcessedEmail): ZIO[Any, SQLException, Unit] =
    run(query[ProcessedEmail].insertValue(lift(processed))).unit

end ProcessedEmailRepositoryLive

object ProcessedEmailRepositoryLive:
  val layer = ZLayer.fromFunction(new ProcessedEmailRepositoryLive(_))
