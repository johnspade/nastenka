package ru.johnspade.nastenka.email

import zio.*
import com.github.kklisura.cdt.services.ChromeService
import com.github.kklisura.cdt.launch.ChromeLauncher

object ChromeServiceLive:
  val layer: ZLayer[Any, Throwable, ChromeService] =
    ZLayer.scoped(
      ZIO
        .acquireRelease(ZIO.attemptBlocking(new ChromeLauncher())) { launcher =>
          ZIO.attemptBlocking(launcher.close()).orDie
        }
        .map(_.launch(true))
    )
