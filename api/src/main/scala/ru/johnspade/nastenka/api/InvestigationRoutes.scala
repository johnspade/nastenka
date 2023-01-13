package ru.johnspade.nastenka.api

import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.InvestigationsResponse
import ru.johnspade.nastenka.models.NewInvestigation
import ru.johnspade.nastenka.models.UpdatedInvestigation
import zhttp.http.*
import zio.*
import zio.json.*

import java.util.UUID

class InvestigationRoutes(apiService: ApiInvestigationService):
  val routes: Http[Any, Throwable, Request, Response] = Http.collectZIO[Request] {
    case Method.GET -> !! / "investigations" =>
      apiService.getAll
        .map(investigations => Response.json(InvestigationsResponse(investigations).toJson))

    case Method.GET -> !! / "investigations" / id =>
      apiService
        .getFull(UUID.fromString(id))
        .map(investigation => Response.json(investigation.toJson))

    case req @ Method.POST -> !! / "investigations" =>
      for
        bodyAsString <- req.body.asString.orDie
        newInvestigation <- ZIO
          .fromEither(bodyAsString.fromJson[NewInvestigation])
          .mapError(msg => new RuntimeException(msg)) // todo handle errors
          .orDie
        investigation <- apiService.create(newInvestigation)
      yield Response.json(investigation.toJson)

    case req @ Method.PUT -> !! / "investigations" / id =>
      for
        bodyAsString <- req.body.asString.orDie
        investigation <- ZIO
          .fromEither(bodyAsString.fromJson[UpdatedInvestigation])
          .mapError(msg => new RuntimeException(msg)) // todo handle errors
          .orDie
        investigation <- apiService.save(UUID.fromString(id), investigation)
      yield Response.json(investigation.toJson)

    case Method.GET -> !! / "investigations" / investigationId / "pins" / pinId =>
      apiService
        .getPin(UUID.fromString(pinId))
        .map(pin => Response.json(pin.toJson))

    case Method.DELETE -> !! / "investigations" / id =>
      apiService.delete(UUID.fromString(id)).as(Response.status(Status.NoContent))
  }

object InvestigationRoutes:
  val layer = ZLayer.fromFunction(new InvestigationRoutes(_))
