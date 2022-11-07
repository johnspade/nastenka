package ru.johnspade.nastenka

import com.raquo.laminar.api.L.{Owner => _, *}

object MainPage {
  def body: Div =
    div(
      cls("mx-auto max-w-2xl space-y-4 mt-6"),
      child <-- Router.router.$currentPage.map {
        case Page.InvestigationsPage => new InvestigationIndexView().body
        case Page.HomePage           => new InvestigationIndexView().body
        case _                       => ???
      }
    )
}
