package ru.johnspade.nastenka

import com.raquo.laminar.api.L.*
import com.raquo.laminar.modifiers.Binder
import com.raquo.waypoint.*
import zio.json.*

import java.util.UUID

enum Page(val title: Option[String]):
  case InvestigationsPage                                              extends Page(Some("Investigations"))
  case InvestigationPage(id: UUID, investigationTitle: Option[String]) extends Page(investigationTitle)
  case HomePage                                                        extends Page(Some("Nastenka"))

object Page:
  given investigationsPageJsonCodec: JsonCodec[InvestigationsPage.type] = DeriveJsonCodec.gen
  given investigationPageJsonCodec: JsonCodec[InvestigationPage]        = DeriveJsonCodec.gen
  given homePageJsonCodec: JsonCodec[HomePage.type]                     = DeriveJsonCodec.gen
  given pageJsonCodec: JsonCodec[Page]                                  = DeriveJsonCodec.gen

object Router:
  import Page.*

  val homeRoute: Route[HomePage.type, Unit] =
    Route.static(HomePage, root / endOfSegments)

  val investigationsRoute: Route[InvestigationsPage.type, Unit] =
    Route.static(InvestigationsPage, root / "investigations" / endOfSegments)

  val investigationRoute: Route[InvestigationPage, String] =
    Route(
      encode = (page: InvestigationPage) => page.id.toString,
      decode = (id: String) => InvestigationPage(UUID.fromString(id), investigationTitle = None),
      pattern = root / "investigations" / segment[String] / endOfSegments
    )

  val router = new Router[Page](
    routes = List(homeRoute, investigationsRoute, investigationRoute),
    getPageTitle = _.title.getOrElse("Nastenka"),
    serializePage = page => page.toJson,
    deserializePage = pageStr => pageStr.fromJson[Page].getOrElse(HomePage)
  )(
    $popStateEvent = windowEvents.onPopState,
    owner = unsafeWindowOwner
  )

  def navigateTo(page: Page): Binder[HtmlElement] = Binder { el =>

    val isLinkElement = el.ref.isInstanceOf[org.scalajs.dom.html.Anchor]

    if (isLinkElement) {
      el.amend(href(router.absoluteUrlForPage(page)))
    }

    // If element is a link and user is holding a modifier while clicking:
    //  - Do nothing, browser will open the URL in new tab / window / etc. depending on the modifier key
    // Otherwise:
    //  - Perform regular pushState transition
    (onClick
      .filter(ev => !(isLinkElement && (ev.ctrlKey || ev.metaKey || ev.shiftKey || ev.altKey)))
      .preventDefault
      --> (_ => router.pushState(page))).bind(el)
  }
