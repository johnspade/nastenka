package ru.johnspade.nastenka.frontend

import com.raquo.laminar.api.L.*
import com.raquo.waypoint.SplitRender
import ru.johnspade.nastenka.frontend.views.*
import ru.johnspade.nastenka.models.Investigation
import ru.johnspade.nastenka.models.NewInvestigation
import ru.johnspade.nastenka.models.UpdatedInvestigation

object MainPage extends Component:
  import Page.*

  private val splitter = SplitRender[Page, HtmlElement](Router.router.currentPageSignal)
    .collectSignal[InvestigationPage] { $page => new InvestigationView($page).body }
    .collectStatic(HomePage)(div())

  private val loadInvestigationsEventBus  = new EventBus[Unit]
  private val deleteInvestigationEventBus = new EventBus[Investigation]
  private val saveInvestigationEventBus   = new EventBus[UpdatedInvestigation]
  private val deleteInvestigationStream =
    deleteInvestigationEventBus.events
      .flatMap(inv => Requests.deleteInvestigation(inv.id))
  private val saveInvestigationStream =
    saveInvestigationEventBus.events.flatMap(inv => Requests.saveInvestigation(inv.id, inv))
  private val loadInvestigationsAfterSave = saveInvestigationStream.map(_ => ())

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

  private val investigationContext: Var[Option[Investigation]] = Var(None)

  private val showContextMenu              = Var(false)
  private val showCreateInvestigationPopup = Var(false)
  private val showRenameInvestigationPopup = Var(false)

  private val newInvestigationTitle = Var("")
  private val investigationTitle    = Var("")

  private val investigationContextMenu = div(
    cls("absolute z-50 bg-white rounded shadow-xl py-2 mt-2"),
    cls <-- showContextMenu.signal.map(show => if show then "block" else "hidden"),
    idAttr("context-menu"),
    styleAttr := "top: 0; left: 0;",
    a(
      cls := "block px-4 py-2 text-gray-800 hover:bg-gray-200",
      "Rename",
      onClick --> { _ =>
        showContextMenu.set(false)
        investigationTitle.set(investigationContext.now().fold("")(_.title))
        showRenameInvestigationPopup.set(true)
      }
    ),
    a(
      cls := "block px-4 py-2 text-gray-800 hover:bg-gray-200",
      "Delete",
      onClick --> { _ =>
        investigationContext.now().foreach(inv => deleteInvestigationEventBus.emit(inv))
        Router.router.pushState(HomePage)
      }
    )
  )

  def body: Div =
    div(
      deleteInvestigationStream --> loadInvestigationsEventBus.writer,
      loadInvestigationsAfterSave --> loadInvestigationsEventBus.writer,
      cls("md:grid md:grid-cols-6 md:h-screen flex flex-col overflow-auto"),
      div(
        cls("md:col-span-1 flex bg-gray-100"),
        navTag(
          cls("grow min-w-0"),
          div(
            cls("truncate"),
            a(
              Router.navigateTo(HomePage),
              h1(
                cls("text-4xl bold uppercase text-gray-700 tracking-wide p-4 text-center md:text-left"),
                "Nastenka"
              ),
              onClick --> { _ => loadInvestigationsEventBus.emit(()) }
            )
          ),
          button(
            cls("ml-4 font-bold py-2 px-4 rounded bg-gray-200 hover:bg-gray-400 text-gray-700"),
            "Create investigation",
            onClick --> { _ => showCreateInvestigationPopup.set(true) }
          ),
          div(
            ul(
              cls("mt-4"),
              children <-- $loadedInvestigations.split(_.id) { (id, initialInvestigation, invStream) =>
                li(
                  cls("text-lg"),
                  cls <-- Router.router.currentPageSignal.map { page =>
                    page match
                      case Page.InvestigationPage(selectedId, _) if id == selectedId =>
                        "bg-gray-400 text-black font-bold"
                      case _ => "text-gray-700 hover:bg-gray-200"
                  },
                  a(
                    cls("block flex items-center p-2"),
                    Router.navigateTo(Page.InvestigationPage(id, None)),
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
                      child.text <-- invStream.map(_.title)
                    )
                  ),
                  onContextMenu --> { e => e.preventDefault() },
                  composeEvents(onContextMenu) {
                    _.withCurrentValueOf(invStream)
                  } --> { case (e, inv) =>
                    investigationContext.set(Some(inv))
                    val menuRef = investigationContextMenu.ref
                    showContextMenu.set(true)
                    menuRef.style.top = s"${e.pageY}px"
                    menuRef.style.left = s"${e.pageX}px"
                  }
                )
              }
            ),
            investigationContextMenu,
            documentEvents(_.onClick) --> { e =>
              val menuRef = investigationContextMenu.ref
              val target  = e.target
              if target.isInstanceOf[Element] && target.asInstanceOf[Element].ref.id == menuRef.id then
                e.preventDefault()
              else showContextMenu.set(false)
            }
          )
        )
      ),
      div(
        cls("md:col-span-5 p-4 grow overflow-auto"),
        child <-- splitter.signal
      ),
      div(
        cls := "fixed top-0 left-0 right-0 bottom-0 flex items-center justify-center z-50",
        cls <-- showCreateInvestigationPopup.signal.map(show => if show then "block" else "hidden"),
        div(
          cls := "bg-white rounded-lg p-6 shadow-xl",
          form(
            label(
              cls := "block font-bold mb-2 text-lg",
              "Title"
            ),
            input(
              tpe := "text",
              cls := "border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline",
              controlled(
                value <-- newInvestigationTitle,
                onInput.mapToValue --> newInvestigationTitle
              )
            ),
            div(
              cls("flex items-center gap-2"),
              button(
                cls := "bg-gray-200 hover:bg-gray-400 font-bold mt-4 py-2 px-4 rounded focus:outline-none focus:shadow-outline",
                tpe := "submit",
                "Submit",
                onClick.preventDefault --> { _ =>
                  Requests
                    .createInvestigation(NewInvestigation(newInvestigationTitle.now()))
                    .foreach(_ => loadInvestigationsEventBus.emit(()))(unsafeWindowOwner)
                  newInvestigationTitle.set("")
                  showCreateInvestigationPopup.set(false)
                }
              ),
              button(
                cls := "bg-gray-200 hover:bg-gray-400 font-bold mt-4 py-2 px-4 rounded focus:outline-none focus:shadow-outline",
                "Cancel",
                onClick.preventDefault --> { _ =>
                  newInvestigationTitle.set("")
                  showCreateInvestigationPopup.set(false)
                }
              )
            )
          )
        )
      ),
      div(
        cls := "fixed top-0 left-0 right-0 bottom-0 flex items-center justify-center z-50",
        cls <-- showRenameInvestigationPopup.signal.map(show => if show then "block" else "hidden"),
        div(
          cls := "bg-white rounded-lg p-6 shadow-xl",
          form(
            label(
              cls := "block font-bold mb-2 text-lg",
              "Title"
            ),
            input(
              tpe := "text",
              cls := "border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline",
              controlled(
                value <-- investigationTitle,
                onInput.mapToValue --> investigationTitle
              )
            ),
            div(
              cls("flex items-center gap-2"),
              button(
                cls := "bg-gray-200 hover:bg-gray-400 font-bold mt-4 py-2 px-4 rounded focus:outline-none focus:shadow-outline",
                tpe := "submit",
                "Save",
                onClick.preventDefault --> { _ =>
                  investigationContext.now().foreach { inv =>
                    saveInvestigationEventBus
                      .emit(UpdatedInvestigation(inv.id, investigationTitle.now(), inv.pinsOrder))
                  }
                  investigationTitle.set("")
                  showRenameInvestigationPopup.set(false)
                }
              ),
              button(
                cls := "bg-gray-200 hover:bg-gray-400 font-bold mt-4 py-2 px-4 rounded focus:outline-none focus:shadow-outline",
                "Cancel",
                onClick.preventDefault --> { _ =>
                  investigationTitle.set("")
                  showRenameInvestigationPopup.set(false)
                }
              )
            )
          )
        )
      )
    )
