package ru.johnspade.nastenka.api

import ru.johnspade.nastenka.errors.InvestigationNotFound
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.InvestigationsResponse
import ru.johnspade.nastenka.models.NewInvestigation
import ru.johnspade.nastenka.models.Problem
import ru.johnspade.nastenka.models.UpdatedInvestigation
import zio.http.*
import zio.*
import zio.json.*

import java.util.UUID

class InvestigationRoutes(apiService: ApiInvestigationService):
  private val Prefix = "api"

  val routes: Http[Any, Throwable, Request, Response] = Http.collectZIO[Request] {
    case Method.GET -> Root / Prefix / "investigations" =>
      toResponse(apiService.getAll)(investigations => Response.json(InvestigationsResponse(investigations).toJson))

    case Method.GET -> Root / Prefix / "investigations" / id =>
      toResponse(
        apiService
          .getFull(UUID.fromString(id))
      )(investigation => Response.json(investigation.toJson))

    case req @ Method.POST -> Root / Prefix / "investigations" =>
      toResponse(for
        bodyAsString <- req.body.asString.orDie
        newInvestigation <- ZIO
          .fromEither(bodyAsString.fromJson[NewInvestigation])
          .mapError(msg => new RuntimeException(msg)) // todo handle errors
          .orDie
        investigation <- apiService.create(newInvestigation)
      yield investigation)(investigation => Response.json(investigation.toJson))

    case req @ Method.PUT -> Root / Prefix / "investigations" / id =>
      toResponse(for
        bodyAsString <- req.body.asString.orDie
        investigation <- ZIO
          .fromEither(bodyAsString.fromJson[UpdatedInvestigation])
          .mapError(msg => new RuntimeException(msg)) // todo handle errors
          .orDie
        investigation <- apiService.save(UUID.fromString(id), investigation)
      yield investigation)(investigation => Response.json(investigation.toJson))

    case Method.GET -> Root / Prefix / "investigations" / investigationId / "pins" / pinId =>
      toResponse(
        apiService
          .getPin(UUID.fromString(pinId))
      )(pin => Response.json(pin.toJson))

    case Method.DELETE -> Root / Prefix / "investigations" / id =>
      toResponse(apiService.delete(UUID.fromString(id)))(_ => Response.status(Status.NoContent))

    case Method.DELETE -> Root / Prefix / "investigations" / investigationId / "pins" / pinId =>
      toResponse(apiService.deletePin(UUID.fromString(pinId), UUID.fromString(investigationId)))(_ =>
        Response.status(Status.NoContent)
      )
  }

object InvestigationRoutes:
  val layer = ZLayer.fromFunction(new InvestigationRoutes(_))

private final case class ErrorResponse(status: Status, problem: Problem)

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
      Response(
        errorResponse.status,
        Headers("Content-Type", "application/problem+json"),
        Body.fromString(errorResponse.problem.toJson)
      )
    }
    .map(f)
    .merge
