package ru.johnspade.nastenka

import zio.*

object Main extends ZIOAppDefault:
  def run: Task[Unit] =
    ZIO
      .serviceWithZIO[NastenkaServer](_.start)
      .provide(NastenkaServer.layer, InvestigationRoutes.layer)
