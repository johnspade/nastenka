package ru.johnspade.nastenka

import zhttp.http.*
import zio.*
import zio.json.*

import java.util.UUID

class InvestigationRoutes(investigationRepo: InvestigationRepository):
  val routes: Http[Any, Throwable, Request, Response] = Http.collectZIO[Request] {
    case Method.GET -> !! / "investigations" =>
      investigationRepo.getAll.orDie
        .map(investigations => Response.json(InvestigationsResponse(investigations).toJson))

    case Method.GET -> !! / "investigations" / id =>
      investigationRepo
        .getFull(UUID.fromString(id))
        .orDie
        .map(investigation => Response.json(investigation.toJson))
  }

object InvestigationRoutes:
  val layer: ZLayer[InvestigationRepository, Nothing, InvestigationRoutes] =
    ZLayer {
      for investigationRepo <- ZIO.service[InvestigationRepository]
      yield new InvestigationRoutes(investigationRepo)
    }
