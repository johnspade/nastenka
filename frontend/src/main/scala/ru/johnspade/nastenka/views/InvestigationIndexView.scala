package ru.johnspade.nastenka.views

import com.raquo.laminar.api.L.*
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.Component
import ru.johnspade.nastenka.Requests
import ru.johnspade.nastenka.Router
import ru.johnspade.nastenka.Page

final class InvestigationIndexView extends Component:
  val loadInvestigationsEventBus = new EventBus[Unit]

  val $loadedInvestigations: Signal[List[Investigation]] =
    EventStream
      .merge(
        Requests.getAllInvestigations,
        loadInvestigationsEventBus.events.flatMap(_ => Requests.getAllInvestigations)
      )
      .map(_.reverse)
      .toSignal(List.empty)

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
        children <-- $loadedInvestigations.split(_.id) { (id, investigation, _) =>
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
