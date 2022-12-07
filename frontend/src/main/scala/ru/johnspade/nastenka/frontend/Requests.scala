package ru.johnspade.nastenka.frontend

import com.raquo.laminar.api.L.*
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.InvestigationFullModel
import ru.johnspade.nastenka.models.InvestigationsResponse
import ru.johnspade.nastenka.models.PinModel
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

  def getAllInvestigations: EventStream[List[Investigation]] =
    getRequest[InvestigationsResponse]("investigations").map(_.investigations)

  def getInvestigationFull(id: UUID): EventStream[InvestigationFullModel] =
    getRequest[InvestigationFullModel]("investigations", id)

  def getPin(investigationId: UUID, pinId: UUID): EventStream[PinModel] =
    getRequest[PinModel]("investigations", investigationId, "pins", pinId)

end Requests
