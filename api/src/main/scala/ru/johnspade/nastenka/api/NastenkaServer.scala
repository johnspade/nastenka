package ru.johnspade.nastenka.api

import zio.http.*
import zio.*
import zio.stream.ZStream

import java.io.IOException

class NastenkaServer(investigationRoutes: InvestigationRoutes):
  private val allRoutes: HttpApp[Any, Throwable] = investigationRoutes.routes

  def start: ZIO[Any, Throwable, Unit] =
    for {
      port <- System.envOrElse("PORT", "8080").map(_.toInt)
      _    <- Server.serve(allRoutes.withDefaultErrorResponse).provide(Server.defaultWithPort(port))
    } yield ()

object NastenkaServer:
  val layer: URLayer[InvestigationRoutes, NastenkaServer] = ZLayer {
    for {
      investigationRoutes <- ZIO.service[InvestigationRoutes]
    } yield new NastenkaServer(investigationRoutes)
  }
