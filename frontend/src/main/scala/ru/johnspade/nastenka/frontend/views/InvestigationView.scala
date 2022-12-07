package ru.johnspade.nastenka.frontend.views

import com.raquo.laminar.api.L.*
import org.scalajs.dom.HTMLIFrameElement
import ru.johnspade.nastenka.frontend.Component
import ru.johnspade.nastenka.frontend.Page
import ru.johnspade.nastenka.frontend.Requests
import ru.johnspade.nastenka.frontend.Router
import ru.johnspade.nastenka.models.InvestigationFull
import ru.johnspade.nastenka.models.Pin
import ru.johnspade.nastenka.models.PinType
import ru.johnspade.nastenka.models.PinType.*

import java.util.UUID

final class InvestigationView(investigationPage: Signal[Page.InvestigationPage]) extends Component:
  private val selectedPin: Var[Option[Pin]] = Var(None)

  private val investigation: EventStream[InvestigationFull] =
    investigationPage.flatMap(page => Requests.getInvestigationFull(page.id))
  private val pinIsSelected = selectedPin.signal.map(_.isDefined)

  private val deselectPin = selectedPin.writer.contramap[InvestigationFull](_ => None)

  override def body: Div = div(
    cls("h-full flex flex-col"),
    p(
      cls("text-6xl bold text-gray-700 text-center md:text-left"),
      child <-- investigation.map(_.title)
    ),
    p(
      cls("text-xl text-gray-300 text-center md:text-left"),
      "nastenka@ilopatin.ru"
    ),
    div(
      cls("flex md:grid md:grid-cols-5 grow"),
      div(
        cls("md:block md:col-span-2"),
        cls.toggle("hidden") <-- pinIsSelected,
        div(
          cls("flex flex-col grow w-full gap-2 max-w-screen-md mt-2"),
          children <-- investigation.map(inv =>
            inv.pins.map { pin =>
              div(
                investigation --> deselectPin,
                cls("p-4 bg-white rounded-md shadow-lg flex flex-col"),
                cls.toggle("bg-gray-200") <-- selectedPin.signal.map(_.exists(_.id == pin.id)),
                div(
                  cls("flex flex-row space-x-2 items-center"),
                  div(
                    cls("shrink-0"),
                    getPinTypeIcon(pin.pinType)
                  ),
                  p(
                    cls("text-gray-500"),
                    pin.sender.getOrElse("")
                  )
                ),
                div(
                  cls("text-xl line-clamp-1"),
                  pin.title.getOrElse("(no title)")
                ),
                div(
                  cls("line-clamp-2"),
                  p(
                    pin.text.getOrElse("")
                  )
                ),
                onClick --> { _ => selectedPin.set(Some(pin)) }
              )
            }
          )
        )
      ),
      div(
        cls("md:col-span-3 flex flex-col grow p-2 md:px-4 space-y-2"),
        children <-- selectedPin.signal.map {
          case Some(pin) =>
            List(
              button(
                cls("font-bold py-2 px-4 rounded bg-gray-200 text-gray-700 md:hidden"),
                onClick --> { _ => selectedPin.set(None) },
                "← Back"
              ),
              p(
                cls("text-2xl"),
                pin.title.getOrElse("(no title)")
              ),
              div(
                cls("flex flex-row space-x-2 items-center"),
                div(
                  cls("shrink-0"),
                  getPinTypeIcon(pin.pinType)
                ),
                p(
                  cls("text-gray-500"),
                  pin.sender.getOrElse("")
                )
              ),
              p(
                cls(""),
                pin.text.getOrElse("")
              ),
              div(
                idAttr("pin-html"),
                cls("grow"),
                iframe(
                  cls("w-full h-full mt-4"),
                  onLoad --> { e =>
                    val f = e.target.asInstanceOf[HTMLIFrameElement]
                    f.contentWindow.document.body.innerHTML = pin.original.getOrElse("")
                  }
                )
              )
            )
          case None => List(emptyNode)
        }
      )
    )
  )

  private def getPinTypeIcon(pinType: PinType) =
    pinType match
      case TELEGRAM_MESSAGE =>
        img(
          cls("h-5 w-5"),
          src("/img/telegram_logo.svg")
        )
      case EMAIL =>
        svg.svg(
          svg.cls("w-5 h-5"),
          svg.fill("none"),
          svg.viewBox("0 0 24 24"),
          svg.strokeWidth("1.5"),
          svg.stroke("currentColor"),
          svg.path(
            svg.strokeLineCap("round"),
            svg.strokeLineJoin("round"),
            svg.d(
              "M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75"
            )
          )
        )
      case _ =>
        svg.svg(
          svg.cls("w-5 h-5"),
          svg.fill("none"),
          svg.viewBox("0 0 24 24"),
          svg.strokeWidth("1.5"),
          svg.stroke("currentColor"),
          svg.path(
            svg.strokeLineCap("round"),
            svg.strokeLineJoin("round"),
            svg.d(
              "M18.375 12.739l-7.693 7.693a4.5 4.5 0 01-6.364-6.364l10.94-10.94A3 3 0 1119.5 7.372L8.552 18.32m.009-.01l-.01.01m5.699-9.941l-7.81 7.81a1.5 1.5 0 002.112 2.13"
            )
          )
        )
