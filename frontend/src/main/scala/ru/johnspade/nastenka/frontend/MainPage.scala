package ru.johnspade.nastenka.frontend

import com.raquo.laminar.api.L.*
import com.raquo.waypoint.SplitRender
import ru.johnspade.nastenka.frontend.views.*
import ru.johnspade.nastenka.models.Investigation

object MainPage extends Component:
  import Page.*

  private val splitter = SplitRender[Page, HtmlElement](Router.router.$currentPage)
    .collectSignal[InvestigationPage] { $page => new InvestigationView($page).body }
    .collectStatic(HomePage)(div())

  private val loadInvestigationsEventBus = new EventBus[Unit]

  private val $loadedInvestigations: Signal[List[Investigation]] =
    EventStream
      .merge(
        Requests.getAllInvestigations,
        loadInvestigationsEventBus.events
          .flatMap { _ =>
            Requests.getAllInvestigations
          }
      )
      .map(_.reverse)
      .toSignal(List.empty)

  def body: Div =
    div(
      cls("grid md:grid-cols-4 md:h-screen"),
      div(
        cls("md:col-span-1 flex bg-gray-100"),
        nav(
          cls("grow"),
          div(
            a(
              Router.navigateTo(HomePage),
              h1(
                cls("text-4xl bold uppercase text-gray-700 tracking-wider p-4 text-center md:text-left"),
                "Nastenka"
              ),
              onClick --> { _ => loadInvestigationsEventBus.emit(()) }
            ),
            div()
          ),
          new InvestigationIndexView($loadedInvestigations).body
        )
      ),
      div(
        cls("md:col-span-3 p-4"),
        child <-- splitter.$view
      )
    )
