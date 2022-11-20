package ru.johnspade.nastenka.frontend.views

import com.raquo.laminar.api.L.*
import ru.johnspade.nastenka.frontend.Component
import ru.johnspade.nastenka.frontend.Page
import ru.johnspade.nastenka.frontend.Requests
import ru.johnspade.nastenka.frontend.Router
import ru.johnspade.nastenka.models.InvestigationFull

import java.util.UUID
import ru.johnspade.nastenka.models.PinType
import ru.johnspade.nastenka.models.PinType.*
import ru.johnspade.nastenka.models.PinType

final class InvestigationView($investigationPage: Signal[Page.InvestigationPage]) extends Component:
  private val $investigation: EventStream[InvestigationFull] =
    $investigationPage.flatMap(page => Requests.getInvestigationFull(page.id))

  override def body: Div = div(
    cls("flex flex-col gap-2 flex-grow max-w-screen-md"),
    children <-- $investigation.map(_.pins.map { pin =>
      div(
        cls("p-4 bg-white rounded-md shadow-lg flex flex-col space-y-2"),
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
        pin.title
          .map { title =>
            div(
              cls("text-2xl"),
              title
            )
          }
          .getOrElse(emptyMod),
        div(
          pin.fileKey
            .map { key =>
              a(
                href(s"http://127.0.0.1:9000/nastenka/$key.pdf"),
                cls("underline"),
                key.toString() + ".pdf"
              )
            }
            .getOrElse(
              div()
            )
        ),
        div(
          cls("mt-4"),
          p(
            pin.text.getOrElse("")
          )
        )
      )
    })
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
