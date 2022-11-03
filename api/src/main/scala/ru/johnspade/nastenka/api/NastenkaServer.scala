package ru.johnspade.nastenka.api

import zhttp.http.*
import zhttp.http.middleware.HttpMiddleware
import zhttp.service.Server
import zio.*

class NastenkaServer(investigationRoutes: InvestigationRoutes):
  val allRoutes: HttpApp[Any, Throwable] = investigationRoutes.routes

  def start: ZIO[Any, Throwable, Unit] =
    for {
      port <- System.envOrElse("PORT", "8080").map(_.toInt)
      _    <- Server.start(port, allRoutes @@ Middleware.cors())
    } yield ()

object NastenkaServer:
  val layer: URLayer[InvestigationRoutes, NastenkaServer] = ZLayer {
    for {
      investigationRoutes <- ZIO.service[InvestigationRoutes]
    } yield new NastenkaServer(investigationRoutes)
  }
