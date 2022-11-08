package ru.johnspade.nastenka.views

import ru.johnspade.nastenka.Component
import com.raquo.airstream.eventbus.EventBus

import com.raquo.laminar.api.L.*
import ru.johnspade.nastenka.models.InvestigationFull
import java.util.UUID
import ru.johnspade.nastenka.Requests
import ru.johnspade.nastenka.Router
import ru.johnspade.nastenka.Page

final class InvestigationView($investigationPage: Signal[Page.InvestigationPage]) extends Component:
  val reloadInvestigationBus = new EventBus[Unit]

  val $investigation: EventStream[InvestigationFull] =
    EventStream.merge(
      $investigationPage.flatMap(page => Requests.getInvestigationFull(page.id)),
      reloadInvestigationBus.events
        .flatMap(_ => $investigationPage.flatMap(page => Requests.getInvestigationFull(page.id)))
    )

  override def body: Div = div(
    reloadInvestigationBus.events --> { _ => () },
    onMountCallback(_ => reloadInvestigationBus.emit(())),
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
                src("img/telegram_logo.svg")
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
