package ru.johnspade.nastenka.frontend

import com.raquo.laminar.api.L.*
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.InvestigationFullModel
import ru.johnspade.nastenka.models.InvestigationsResponse
import ru.johnspade.nastenka.models.NewInvestigation
import ru.johnspade.nastenka.models.PinModel
import ru.johnspade.nastenka.models.UpdatedInvestigation
import sttp.client3.*
import zio.json.*

import java.util.UUID
import scala.concurrent.Future

object Requests:
  private val backend: SttpBackend[Future, Any] = FetchBackend()

  private val baseUrl = uri"http://localhost:8080"

  private def getRequest[A: JsonDecoder](path: Any*): EventStream[A] = {
    val request = quickRequest.get(uri"$baseUrl/$path")
    EventStream.fromFuture(backend.send(request)).map { response =>
      response.body.fromJson[A] match {
        case Right(b) => b
        case Left(e)  => throw new Error(s"Error parsing JSON: $e")
      }
    }
  }

  def putRequest[In: JsonEncoder, Out: JsonDecoder](body: In)(path: Any*): EventStream[Out] = {
    val request = quickRequest.put(uri"$baseUrl/$path").body(body.toJson)
    EventStream.fromFuture(backend.send(request)).map { response =>
      response.body.fromJson[Out] match {
        case Right(b) => b
        case Left(e)  => throw new Error(s"Error parsing JSON: $e")
      }
    }
  }

  def postRequest[In: JsonEncoder, Out: JsonDecoder](body: In)(path: Any*): EventStream[Out] = {
    val request = quickRequest.post(uri"$baseUrl/$path").body(body.toJson)
    EventStream.fromFuture(backend.send(request)).map { response =>
      response.body.fromJson[Out] match {
        case Right(b) => b
        case Left(e)  => throw new Error(s"Error parsing JSON: $e")
      }
    }
  }

  def deleteRequest(path: Any*): EventStream[Unit] = {
    val request = quickRequest.delete(uri"$baseUrl/$path")
    EventStream.fromFuture(backend.send(request)).map(_ => ())
  }

  def getAllInvestigations: EventStream[List[Investigation]] =
    getRequest[InvestigationsResponse]("investigations").map(_.investigations)

  def getInvestigationFull(id: UUID): EventStream[InvestigationFullModel] =
    getRequest[InvestigationFullModel]("investigations", id)

  def getPin(investigationId: UUID, pinId: UUID): EventStream[PinModel] =
    getRequest[PinModel]("investigations", investigationId, "pins", pinId)

  def saveInvestigation(
      investigationId: UUID,
      investigation: UpdatedInvestigation
  ): EventStream[InvestigationFullModel] =
    putRequest[UpdatedInvestigation, InvestigationFullModel](investigation)("investigations", investigationId)

  def createInvestigation(investigation: NewInvestigation): EventStream[Investigation] =
    postRequest[NewInvestigation, Investigation](investigation)("investigations")

  def deleteInvestigation(investigationId: UUID): EventStream[Unit] =
    deleteRequest("investigations", investigationId)

end Requests
