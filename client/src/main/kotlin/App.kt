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

    window.addEventListener("load", { _ -> document.location?.run { controller.setView(pathname, hash) } });
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
      div("user-info")
    }
  }
  div("app container") {
    div("side-menu") {
      div("files") {
      }
    }
    div("content") {
      div("document") {
      }
      div("tools") {
        div("tools-btns") {
          div("tool-btn-calendar") {
            +"Agenda"
          }
          div("tool-btn-editor") {
            +"Editor"
          }
        }
        div("calendar") {

        }
        div("editor") {

        }
      }
    }
  }
}
