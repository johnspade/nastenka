package ru.johnspade.nastenka.frontend

import com.raquo.laminar.api.L.*
import ru.johnspade.nastenka.frontend.views.*
import com.raquo.waypoint.SplitRender

object MainPage extends Component:
  import Page.*

  val splitter = SplitRender[Page, HtmlElement](Router.router.$currentPage)
    .collectSignal[InvestigationPage] { $page => new InvestigationView($page).body }
    .collectStatic(InvestigationsPage)(new InvestigationIndexView().body)
    .collectStatic(InvestigationsPage)(new InvestigationIndexView().body)

  def body: Div =
    div(
      cls("mx-auto max-w-2xl mt-6"),
      child <-- splitter.$view
    )
