package ru.johnspade.nastenka

import zhttp.http.*
import zio.*
import zio.json.*

import java.util.UUID

class InvestigationRoutes:
  val routes: Http[Any, Throwable, Request, Response] = Http.collectZIO[Request] {
    case Method.GET -> !! / "investigations" =>
      ZIO.attempt(UUID.randomUUID()).map { id =>
        val investigations = InvestigationList(List(InvestigationItem(id, "2022 Barcelona")))
        Response.json(investigations.toJson)
      }
  }

object InvestigationRoutes:
  val layer: ULayer[InvestigationRoutes] = ZLayer.succeed(new InvestigationRoutes)
