package ru.johnspade.nastenka.frontend.views

import com.raquo.laminar.api.L.*
import org.scalajs.dom.HTMLIFrameElement
import ru.johnspade.nastenka.frontend.Component
import ru.johnspade.nastenka.frontend.Page
import ru.johnspade.nastenka.frontend.Requests
import ru.johnspade.nastenka.models.Pin
import ru.johnspade.nastenka.models.PinModel

final class PinView($pinPage: Signal[Page.PinPage]) extends Component:
  private val pin: EventStream[PinModel] =
    $pinPage.flatMap(page => Requests.getPin(page.investigationId, page.pinId))
  override def body: Div = div(
    idAttr("pin-content"),
    cls("h-full"),
    child <-- pin.map { pin =>
      iframe(
        cls("w-full h-full"),
        onLoad --> { e =>
          val f = e.target.asInstanceOf[HTMLIFrameElement]
          f.contentWindow.document.body.innerHTML = pin.original.getOrElse("")
        }
      )
    }
  )
