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

  def addPin(investigationId: UUID, pin: Pin): ZIO[Any, Throwable, Unit]

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

  override def addPin(investigationId: UUID, pin: Pin): ZIO[Any, Throwable, Unit] =
    val savePin = run(query[Pin].insertValue(lift(pin)))
    val saveInvestigationPin = run(
      query[InvestigationPin].insertValue(lift(InvestigationPin(investigationId, pin.id)))
    )
    transaction(
      for
        investigation <- run(quote(query[Investigation].filter(_.id == lift(investigationId)).forUpdate)).map(_.head)
        updatedInvestigation = investigation.copy(pinsOrder = investigation.pinsOrder :+ pin.id)
        _ <- savePin
        _ <- saveInvestigationPin
        _ <- update(updatedInvestigation)
      yield ()
    )

  extension [T](q: Query[T]) {
    inline def forUpdate = quote(sql"$q for update".as[Query[T]])
  }

end InvestigationRepositoryLive

object InvestigationRepositoryLive:
  val layer = ZLayer.fromFunction(new InvestigationRepositoryLive(_))
