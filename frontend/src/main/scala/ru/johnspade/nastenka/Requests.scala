package ru.johnspade.nastenka

import com.raquo.laminar.api.L.*
import sttp.client3.*
import zio.json.*

import scala.concurrent.Future
import ru.johnspade.nastenka.models.InvestigationsResponse

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

  def getAllInvestigations: EventStream[InvestigationsResponse] = getRequest[InvestigationsResponse]("investigations")

end Requests
