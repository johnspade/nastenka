package ru.johnspade.nastenka

import zhttp.http.*
import zio.*
import zio.json.*

import java.util.UUID

class InvestigationRoutes(investigationRepo: InvestigationRepository):
  val routes: Http[Any, Throwable, Request, Response] = Http.collectZIO[Request] {
    case Method.GET -> !! / "investigations" =>
      investigationRepo.getInvestigations.orDie
        .map(investigations => Response.json(InvestigationList(investigations).toJson))
  }

object InvestigationRoutes:
  val layer: ZLayer[InvestigationRepository, Nothing, InvestigationRoutes] =
    ZLayer {
      for investigationRepo <- ZIO.service[InvestigationRepository]
      yield new InvestigationRoutes(investigationRepo)
    }
