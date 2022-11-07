package ru.johnspade.nastenka

import com.raquo.laminar.api.L
import com.raquo.waypoint.*
import zio.json.*

import java.util.UUID

enum Page:
  case InvestigationsPage
  case InvestigationPage(id: UUID)
  case HomePage

object Page:
  given jsonCodec: JsonCodec[Page] = DeriveJsonCodec.gen

object Router:
  import Page.*

  val homeRoute: Route[HomePage.type, Unit] =
    Route.static(HomePage, root / endOfSegments)

  val investigationsRoute: Route[InvestigationsPage.type, Unit] =
    Route.static(InvestigationsPage, root / "investigations" / endOfSegments)

  val investigationRoute: Route[InvestigationPage, String] =
    Route(
      encode = (page: InvestigationPage) => page.id.toString,
      decode = (id: String) => InvestigationPage(UUID.fromString(id)),
      pattern = root / "investigations" / segment[String] / endOfSegments
    )

  val router = new Router[Page](
    routes = List(homeRoute, investigationsRoute, investigationRoute),
    getPageTitle = _.toString, // todo title
    serializePage = page => page.toJson,
    deserializePage = pageStr => pageStr.fromJson[Page].getOrElse(HomePage)
  )(
    $popStateEvent = L.windowEvents.onPopState,
    owner = L.unsafeWindowOwner
  )
