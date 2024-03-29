package ru.johnspade.nastenka.api

import io.getquill.*
import io.getquill.jdbczio.Quill
import ru.johnspade.nastenka.errors.InvestigationNotFound
import ru.johnspade.nastenka.errors.PinNotFound
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.InvestigationFull
import ru.johnspade.nastenka.models.InvestigationPin
import ru.johnspade.nastenka.models.Pin
import ru.johnspade.nastenka.models.PinType
import ru.johnspade.nastenka.models.UpdatedInvestigation
import ru.johnspade.nastenka.persistence.InvestigationRepository
import ru.johnspade.nastenka.persistence.codecs.given
import zio.*

import java.sql.SQLException
import java.util.UUID
import scala.collection.Factory

trait ApiInvestigationRepository:
  def create(investigation: Investigation): ZIO[Any, SQLException, Investigation]

  def getAll: ZIO[Any, SQLException, List[Investigation]]

  def get(id: UUID): ZIO[Any, SQLException, Investigation]

  def getFull(id: UUID): ZIO[Any, InvestigationNotFound | SQLException, InvestigationFull]

  def update(id: UUID, investigation: UpdatedInvestigation): ZIO[Any, SQLException, Investigation]

  def getPin(pinId: UUID): ZIO[Any, PinNotFound | SQLException, Pin]

  def deletePin(pinId: UUID, investigationId: UUID): ZIO[Any, SQLException | Throwable, Unit]

  def delete(id: UUID): ZIO[Any, SQLException, Unit]

class ApiInvestigationRepositoryLive(
    investigationRepo: InvestigationRepository,
    quill: Quill.Postgres[CompositeNamingStrategy2[SnakeCase, PluralizedTableNames]]
) extends ApiInvestigationRepository:
  import quill._

  private given arrayUuidDecoder[Col <: Seq[UUID]](using bf: Factory[UUID, Col]): Decoder[Col] =
    arrayRawDecoder[UUID, Col]
  private given arrayUuidEncoder[Col <: Seq[UUID]]: Encoder[Col] = arrayRawEncoder[UUID, Col]("uuid")

  override def create(investigation: Investigation): ZIO[Any, SQLException, Investigation] =
    run(query[Investigation].insertValue(lift(investigation))).as(investigation)

  override def getAll: ZIO[Any, SQLException, List[Investigation]] = investigationRepo.getAll

  override def get(id: UUID): ZIO[Any, SQLException, Investigation] = investigationRepo.get(id)

  override def getFull(id: UUID): ZIO[Any, InvestigationNotFound | SQLException, InvestigationFull] =
    run(
      query[Investigation]
        .filter(i => i.id == lift(id) && !i.deleted)
        .leftJoin(query[InvestigationPin])
        .on { case (investigation, investigationPin) =>
          investigationPin.investigationId == investigation.id
        }
        .leftJoin(query[Pin])
        .on { case ((_, investigationPinOpt), pin) =>
          investigationPinOpt.map(_.pinId).contains(pin.id) && !pin.deleted
        }
    )
      .map {
        _.groupBy(_._1._1).map { case (investigation, investigationPinsList) =>
          InvestigationFull(
            investigation.id,
            investigation.createdAt,
            investigation.title,
            investigationPinsList.flatMap(_._2),
            investigation.pinsOrder
          )
        }.headOption

      }
      .some
      .mapError(_.getOrElse(InvestigationNotFound(id)))

  override def update(id: UUID, investigation: UpdatedInvestigation): ZIO[Any, SQLException, Investigation] =
    investigationRepo.update(id, investigation)

  override def getPin(pinId: UUID): ZIO[Any, PinNotFound | SQLException, Pin] =
    run(query[Pin].filter(p => p.id == lift(pinId) && !p.deleted))
      .map(_.headOption)
      .some
      .mapError(_.getOrElse(PinNotFound(pinId)))

  override def deletePin(pinId: UUID, investigationId: UUID): ZIO[Any, SQLException | Throwable, Unit] =
    val deletePinQuery = run(query[Pin].filter(_.id == lift(pinId)).update(_.deleted -> lift(true)))
    val removePinFromOrder = run(
      query[Investigation]
        .filter(i => i.id == lift(investigationId))
        .update(_.pinsOrder -> infix"array_remove(pins_order, ${lift(pinId)})::uuid[]".as[Seq[UUID]])
    )

    transaction {
      deletePinQuery *> removePinFromOrder
    }.unit

  override def delete(id: UUID): ZIO[Any, SQLException, Unit] = run(
    query[Investigation].filter(_.id == lift(id)).update(_.deleted -> lift(true))
  ).unit
end ApiInvestigationRepositoryLive

object ApiInvestigationRepositoryLive:
  val layer = ZLayer.fromFunction(new ApiInvestigationRepositoryLive(_, _))
