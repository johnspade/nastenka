package ru.johnspade.nastenka.frontend.views

import com.raquo.laminar.api.L.*
import ru.johnspade.nastenka.frontend.Component
import ru.johnspade.nastenka.frontend.Page
import ru.johnspade.nastenka.frontend.Requests
import ru.johnspade.nastenka.frontend.Router
import ru.johnspade.nastenka.models.Investigation

final class InvestigationIndexView extends Component:
  private val $investigations: Signal[List[Investigation]] =
    Requests.getAllInvestigations.toSignal(List.empty)

  override def body: Div =
    div(
      div(
        cls("text-center sm:text-left"),
        p(
          cls("text-4xl font-semibold text-slate-500"),
          "Investigations"
        )
      ),
      div(
        cls("flex flex-col space-y-2"),
        children <-- $investigations.split(_.id) { (id, investigation, _) =>
          a(
            Router.navigateTo(Page.InvestigationPage(id, Some(investigation.title))),
            p(
              cls("p-4 shadow-lg bg-white rounded-md"),
              investigation.title
            )
          )
        }
      )
    )

end InvestigationIndexView
