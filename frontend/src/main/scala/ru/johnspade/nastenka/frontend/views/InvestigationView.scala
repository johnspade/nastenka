package ru.johnspade.nastenka.frontend.views

import com.raquo.laminar.api.L.*
import ru.johnspade.nastenka.frontend.Component
import ru.johnspade.nastenka.frontend.Page
import ru.johnspade.nastenka.frontend.Requests
import ru.johnspade.nastenka.frontend.Router
import ru.johnspade.nastenka.models.InvestigationFull

import java.util.UUID

final class InvestigationView($investigationPage: Signal[Page.InvestigationPage]) extends Component:
  private val $investigation: EventStream[InvestigationFull] =
    $investigationPage.flatMap(page => Requests.getInvestigationFull(page.id))

  override def body: Div = div(
    div(
      cls("text-center sm:text-left"),
      a(
        Router.navigateTo(Page.InvestigationsPage),
        cls("underline"),
        "To investigations"
      )
    ),
    div(
      cls("text-center sm:text-left"),
      p(
        cls("text-4xl font-semibold text-slate-500"),
        child.text <-- $investigation.map(_.title)
      )
    ),
    div(
      cls("flex flex-col space-y-2"),
      children <-- $investigation.map(_.pins.map { pin =>
        div(
          cls("p-4 bg-white rounded-md shadow-lg flex flex-col"),
          div(
            cls("flex flex-row space-x-4 items-center"),
            div(
              cls("shrink-0"),
              img(
                cls("h-12 w-12"),
                src("/img/telegram_logo.svg")
              )
            ),
            p(
              cls("text-xl text-slate-500"),
              pin.sender.getOrElse("")
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
  )
