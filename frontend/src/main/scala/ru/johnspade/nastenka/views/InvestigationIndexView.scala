package ru.johnspade.nastenka

import com.raquo.laminar.api.L.*
import ru.johnspade.nastenka.models.Investigation

class InvestigationIndexView extends Component:
  val loadInvestigationsEventBus = new EventBus[Unit]

  val $loadedInvestigations: Signal[List[Investigation]] =
    Requests.getAllInvestigations.map(_.investigations).toSignal(List.empty)

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
            href("#"),
            p(
              cls("p-4 shadow-lg bg-white rounded-md"),
              investigation.title
            )
          )
        }
      )
    )

end InvestigationIndexView
