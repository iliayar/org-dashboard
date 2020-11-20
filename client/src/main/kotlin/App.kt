package org.dashboard

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.*
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

class DashboardApp() {

  init {
    val model = Model()
    val template = Template()
    val view = View(template)
    val controller = Controller(model, view)

    val setView: (Event) -> Unit = { _ -> document.location?.run { controller.setView(pathname, hash, false) } }
    window.addEventListener("load", setView);
  }
}


fun main() {
  document.addEventListener("DOMContentLoaded", {
                              val body = document.body as HTMLElement
                              body.append { application() }
                              DashboardApp()
  })
}

fun TagConsumer<*>.application() {
  header {
    div("container") {
      i("fas fa-bars") { id = "menu-toggle-btn" }
      h1() { +"Org Dashboard" }
      div()
    }
  }
  div("content container") {
    div("side-menu") {
      div("files") {
      }
    }
    div("document") {
    }
  }
}
