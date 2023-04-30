package ru.johnspade.nastenka.api

import zio.http.*
import zio.*
import zio.stream.ZStream

import java.io.IOException

class NastenkaServer(investigationRoutes: InvestigationRoutes):
  private val frontendRoutes: Http[Any, Throwable, Request, Response] = Http.collect[Request] {
    case Method.GET -> !! =>
      Response.redirect(URL(!! / "app"), isPermanent = true)

    case Method.GET -> "" /: "app" /: path =>
      Response(
        Status.Ok,
        headers = Headers("Content-Type" -> "text/html; charset=utf-8"),
        body = Body.fromStream(
          ZStream.fromInputStreamZIO(
            ZIO.attemptBlocking(getClass().getResourceAsStream("/static/index.html")).refineToOrDie[IOException]
          )
        )
      )

    case Method.GET -> "" /: "static" /: path =>
      val contentType = Path(Vector(path.segments.last)).toString().reverse.takeWhile(_ != '.').reverse match
        case "js"   => "application/javascript"
        case "css"  => "text/css"
        case "html" => "text/html; charset=utf-8"
        case "svg"  => "image/svg+xml"
        case other =>
          throw new UnsupportedOperationException(
            s"Don't know extension $other"
          )
      Response(
        Status.Ok,
        headers = Headers("Content-Type" -> contentType),
        body = Body.fromStream(
          ZStream.fromInputStreamZIO(
            ZIO.attemptBlocking(getClass().getResourceAsStream(s"/static/$path")).refineToOrDie[IOException]
          )
        )
      )
  }

  private val allRoutes: HttpApp[Any, Throwable] = investigationRoutes.routes ++ frontendRoutes

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
