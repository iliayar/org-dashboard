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
      div("controls") {
        i("fas fa-bars menu-toggle-btn")
        div("btn save-btn") {
          i("fas fa-save")
          +" Save"
        }
        div("btn new-btn") {
          i("fas fa-plus")
          +" New"
        }
        div("btn delete-btn") {
          i("fas fa-trash")
          +" Delete"
        }
      }
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
