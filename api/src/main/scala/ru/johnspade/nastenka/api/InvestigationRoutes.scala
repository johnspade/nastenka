package ru.johnspade.nastenka.api

import ru.johnspade.nastenka.errors.InvestigationNotFound
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
      toResponse(apiService.getAll)(investigations => Response.json(InvestigationsResponse(investigations).toJson))

    case Method.GET -> !! / "investigations" / id =>
      toResponse(
        apiService
          .getFull(UUID.fromString(id))
      )(investigation => Response.json(investigation.toJson))

    case req @ Method.POST -> !! / "investigations" =>
      toResponse(for
        bodyAsString <- req.body.asString.orDie
        newInvestigation <- ZIO
          .fromEither(bodyAsString.fromJson[NewInvestigation])
          .mapError(msg => new RuntimeException(msg)) // todo handle errors
          .orDie
        investigation <- apiService.create(newInvestigation)
      yield investigation)(investigation => Response.json(investigation.toJson))

    case req @ Method.PUT -> !! / "investigations" / id =>
      toResponse(for
        bodyAsString <- req.body.asString.orDie
        investigation <- ZIO
          .fromEither(bodyAsString.fromJson[UpdatedInvestigation])
          .mapError(msg => new RuntimeException(msg)) // todo handle errors
          .orDie
        investigation <- apiService.save(UUID.fromString(id), investigation)
      yield investigation)(investigation => Response.json(investigation.toJson))

    case Method.GET -> !! / "investigations" / investigationId / "pins" / pinId =>
      toResponse(
        apiService
          .getPin(UUID.fromString(pinId))
      )(pin => Response.json(pin.toJson))

    case Method.DELETE -> !! / "investigations" / id =>
      toResponse(apiService.delete(UUID.fromString(id)))(_ => Response.status(Status.NoContent))
  }

object InvestigationRoutes:
  val layer = ZLayer.fromFunction(new InvestigationRoutes(_))

final case class ErrorResponse(status: Status, problem: Problem)

private def convertErrors(e: Throwable): ErrorResponse =
  e match
    case InvestigationNotFound(id) =>
      ErrorResponse(
        Status.NotFound,
        Problem("https://johnspade.ru/problems/investigation-not-found", s"Investigation $id not found")
      )
    case _ =>
      ErrorResponse(
        Status.InternalServerError,
        Problem("https://johnspade.ru/problems/unexpected-error", e.getMessage())
      )

def toResponse[R, E <: Throwable, A](zio: ZIO[R, E, A])(f: A => Response): ZIO[R, Nothing, Response] =
  zio
    .mapError { e =>
      val errorResponse = convertErrors(e)
      Response
        .json(errorResponse.problem.toJson)
        .setStatus(errorResponse.status)
        .withContentType("application/problem+json")
    }
    .map(f)
    .merge
