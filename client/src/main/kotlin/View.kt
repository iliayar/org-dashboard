package org.dashboard

import org.w3c.dom.HTMLElement
import kotlinx.browser.document

class View(private val template: Template) {
  // TODO: Create methods
  val render = Renderer()
  val bind = Binder()

  private val body = document.body as HTMLElement
  private val menu_toggle = qs("#menu-toggle-btn")!!
  private val side_menu = qs(".side-menu")!!
  private val doc_content = qs(".document")!!
  private val files_list = qs(".files", side_menu)!!

  inner class Renderer() {
    fun overview() {
      doc_content.innerHTML = template.overview()
    }

    fun logedOut() {
      doc_content.innerHTML = template.logedOut()
      files_list.innerHTML = "Log in to see files"
    }

    fun doc(content: String) {
      doc_content.innerHTML = content
    }
    fun toggleMenu() {
      if(side_menu.classList.contains("active")) {
        side_menu.classList.remove("active");
      } else {
        side_menu.classList.add("active");
      }
    }
    fun updateFiles(files: List<String>) {
      files_list.innerHTML = template.showFiles(files)
    }
  }

  inner class Binder {
    fun toggleMenu(handler: () -> Unit) {
      menu_toggle.addEventListener("click", { _ -> handler() })
    }
  }
}
