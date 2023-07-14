package ru.johnspade.nastenka.frontend.views

import com.raquo.laminar.api.L.*
import org.scalajs.dom.Element
import org.scalajs.dom.EventTarget
import org.scalajs.dom.HTMLIFrameElement
import org.scalajs.dom.Node
import ru.johnspade.nastenka.frontend.Component
import ru.johnspade.nastenka.frontend.Page
import ru.johnspade.nastenka.frontend.Requests
import ru.johnspade.nastenka.frontend.Router
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.InvestigationFull
import ru.johnspade.nastenka.models.InvestigationFullModel
import ru.johnspade.nastenka.models.PinModel
import ru.johnspade.nastenka.models.PinType
import ru.johnspade.nastenka.models.PinType.*
import ru.johnspade.nastenka.models.UpdatedInvestigation

import java.util.UUID

final class InvestigationView(investigationPage: Signal[Page.InvestigationPage]) extends Component:
  private val selectedPin: Var[Option[PinModel]] = Var(None)

  private val investigation: EventStream[InvestigationFullModel] =
    investigationPage.flatMap(page => Requests.getInvestigationFull(page.id))
  private val pinIsSelected = selectedPin.signal.map(_.isDefined)

  private val deselectPin = selectedPin.writer.contramap[InvestigationFullModel](_ => None)

  private val draggedElement: Var[Option[(Element, Int)]] = Var(None)

  private val pinMovedBus    = new EventBus[(InvestigationFullModel, PinModel, Int)]
  private val pinMovedStream = pinMovedBus.events

  private val saveInvestigationStream = pinMovedStream
    .map { case (investigation, pin, newIndex) =>
      val ids = investigation.pinsOrder
      UpdatedInvestigation(
        investigation.id,
        investigation.title,
        ids.patch(ids.indexOf(pin.id), Vector(), 1).patch(newIndex, Vector(pin.id), 0)
      )
    }
    .flatMap(investigation => Requests.saveInvestigation(investigation.id, investigation))

  private val deletePinEventBus = new EventBus[(PinModel, InvestigationFullModel)]

  private val deletePinStream =
    deletePinEventBus.events.flatMap { case (pin, inv) =>
      Requests.deletePin(pin.id, inv.id).flatMap(_ => Requests.getInvestigationFull(inv.id))
    }

  private val mergedInvestigation = EventStream.merge(
    investigation,
    saveInvestigationStream,
    deletePinStream
  )

  override def body: Div = div(
    pinMovedStream --> draggedElement.updater { case (_, _) => None },
    mergedInvestigation --> deselectPin,
    cls("h-full flex flex-col overflow-auto"),
    div(
      cls("md:block"),
      cls.toggle("hidden") <-- pinIsSelected,
      p(
        cls("text-6xl bold text-gray-700 text-center md:text-left"),
        child <-- mergedInvestigation.map(_.title)
      ),
      p(
        cls("text-xl text-gray-300 text-center md:text-left"),
        child <-- mergedInvestigation.map(_.email)
      )
    ),
    div(
      cls("flex md:grid md:grid-cols-5 grow overflow-auto"),
      div(
        cls("md:block md:col-span-2 overflow-auto"),
        cls.toggle("hidden") <-- pinIsSelected,
        ul(
          cls("flex flex-col grow w-full max-w-screen-md mt-2 divide-y"),
          onDragOver --> { _.preventDefault() },
          children <-- mergedInvestigation
            .map(inv => inv.pins.sortBy(pin => inv.pinsOrder.indexOf(pin.id)).map(pin => (inv, pin)))
            .split(_._2.id) { case (_, (inv, pin), pinStream) =>
              renderPinCard(inv, pin, pinStream.map(_._2))
            }
        )
      ),
      div(
        cls("md:col-span-3 flex flex-col grow p-2 md:px-4 space-y-2 overflow-auto"),
        children <-- selectedPin.signal.map {
          case Some(pin) =>
            List(
              button(
                cls("font-bold py-2 px-4 rounded bg-gray-200 text-gray-700 md:hidden"),
                onClick --> { _ => selectedPin.set(None) },
                "← Back"
              ),
              p(
                cls("text-2xl"),
                pin.title.getOrElse("(no title)")
              ),
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
              renderContent(pin),
              div(pin.url.map { link =>
                a(
                  href(link),
                  target("_blank"),
                  cls("text-blue-500 text-xl"),
                  link
                )
              }),
              div(
                pin.images.map { link =>
                  img(
                    src(link)
                  )
                }
              )
            )
          case None => List(emptyNode)
        }
      )
    )
  )

  private def renderContent(pin: PinModel) =
    pin.html
      .map { htmlBody =>
        div(
          idAttr("pin-html"),
          cls("grow"),
          iframe(
            cls("w-full"),
            onLoad --> { e =>
              val f = e.target.asInstanceOf[HTMLIFrameElement]
              f.contentWindow.document.body.innerHTML = htmlBody
              f.style.height = "0px"
              f.style.height = s"${f.contentWindow.document.body.scrollHeight}px"
            }
          )
        )
      }
      .getOrElse(
        p(
          cls("whitespace-pre-wrap"),
          pin.text.getOrElse("")
        )
      )

  private def getPinTypeIcon(pinType: String) =
    pinType match
      case PinType.TelegramMessage =>
        img(
          cls("h-5 w-5"),
          src("/app/img/telegram_logo.svg")
        )
      case PinType.Email =>
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

  private def renderPinCard(
      initialInvestigation: InvestigationFullModel,
      initialPin: PinModel,
      pinSignal: Signal[PinModel]
  ) =
    val currentInvestigation = Var(initialInvestigation)
    val currentPin           = Var(initialPin)
    li(
      mergedInvestigation --> currentInvestigation.writer,
      pinSignal --> currentPin.writer,
      cls("p-2 flex flex-col cursor-pointer"),
      cls <-- pinSignal
        .combineWithFn(selectedPin) { case (pin, selectedPinOpt) =>
          if selectedPinOpt.exists(_.id == pin.id) then "bg-gray-200" else "bg-white"
        },
      div(
        cls("flex flex-row space-x-2 items-center"),
        div(
          cls("shrink-0"),
          child <-- pinSignal.map(pin => getPinTypeIcon(pin.pinType))
        ),
        p(
          cls("text-gray-500"),
          child.text <-- pinSignal.map(_.sender.getOrElse(""))
        )
      ),
      div(
        cls("text-xl line-clamp-1"),
        child.text <-- pinSignal.map(_.title.getOrElse("(no title)"))
      ),
      div(
        cls("line-clamp-2"),
        p(
          child.text <-- pinSignal.map(_.text.getOrElse(""))
        )
      ),
      div(
        cls("mt-4"),
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
              "M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0"
            )
          ),
          onClick.stopPropagation.compose(_.withCurrentValueOf(pinSignal).map(_._2)) --> { pin =>
            deletePinEventBus.emit(pin -> currentInvestigation.now())
          }
        )
      ),
      onClick.compose(_.withCurrentValueOf(pinSignal).map(_._2)) --> { pin =>
        selectedPin.set(Some(pin))
      },
      draggable(true),
      onDragStart --> { e =>
        e.dataTransfer.effectAllowed = "move"
        e.dataTransfer.setData("text/plain", null)
      },
      onDragStart.compose(_.withCurrentValueOf(pinSignal)) --> { case (e, pin) =>
        val target = e.target.asInstanceOf[Element]
        draggedElement.set(Some((target, currentInvestigation.now().pinsOrder.indexOf(pin))))
      },
      onDragEnd.map { e =>
        val target   = e.target.asInstanceOf[Element]
        val newIndex = target.parentNode.childNodes.filter(_.nodeName == "LI").indexOf(target)
        val inv      = currentInvestigation.now()
        val pin      = currentPin.now()
        (
          inv,
          pin,
          draggedElement.now().map(_._2).getOrElse(inv.pinsOrder.indexOf(pin))
        )
      } --> pinMovedBus.writer,
      onDragOver.preventDefault --> { e =>
        draggedElement
          .now()
          .map { (selected, _) =>
            def getParentLi(el: Node): Node =
              if el.nodeName == "LI" then el else getParentLi(el.parentNode)

            val target = getParentLi(e.target.asInstanceOf[Element]).asInstanceOf[Element]
            val index  = target.parentNode.childNodes.filter(_.nodeName == "LI").indexOf(target)
            draggedElement.set(Some(selected, index))
          }
          .getOrElse(())
      }
    )
