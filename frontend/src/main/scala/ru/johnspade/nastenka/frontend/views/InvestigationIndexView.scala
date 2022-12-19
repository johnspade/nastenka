package ru.johnspade.nastenka.frontend.views

import com.raquo.laminar.api.L.*
import ru.johnspade.nastenka.frontend.Component
import ru.johnspade.nastenka.frontend.Page
import ru.johnspade.nastenka.frontend.Requests
import ru.johnspade.nastenka.frontend.Router
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.frontend.Page

final class InvestigationIndexView(val $investigations: Signal[List[Investigation]]) extends Component:
  override def body =
    div(
      button(
        cls("ml-4 font-bold py-2 px-4 rounded bg-gray-200 text-gray-700"),
        "Create investigation"
      ),
      ul(
        cls("mt-4"),
        children <-- $investigations.split(_.id) { (id, investigation, _) =>
          li(
            cls("text-lg"),
            cls <-- Router.router.$currentPage.map { page =>
              page match
                case Page.InvestigationPage(selectedId, _) if id == selectedId => "bg-gray-400 text-black font-bold"
                case _                                                         => "text-gray-700 hover:bg-gray-200"
            },
            a(
              cls("block flex items-center p-2"),
              Router.navigateTo(Page.InvestigationPage(id, Some(investigation.title))),
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
                    "M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 002.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 00-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 00.75-.75 2.25 2.25 0 00-.1-.664m-5.8 0A2.251 2.251 0 0113.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m0 0H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V9.375c0-.621-.504-1.125-1.125-1.125H8.25zM6.75 12h.008v.008H6.75V12zm0 3h.008v.008H6.75V15zm0 3h.008v.008H6.75V18z"
                  )
                )
              ),
              span(
                cls("ml-2"),
                investigation.title
              )
            )
          )
        }
      )
    )

end InvestigationIndexView
