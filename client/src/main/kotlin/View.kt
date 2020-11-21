package org.dashboard

import org.w3c.dom.*
import kotlinx.browser.document
import kotlinx.browser.window

open class View(private val template: Template) {
  // TODO: Create methods
  val render = Renderer()
  val bind = Binder()
  var loginView: LoginView? = null

  private val body = document.body as HTMLElement
  private val menu_toggle = qs("#menu-toggle-btn")!!
  private val side_menu = qs(".side-menu")!!
  private val content = qs(".content")!!
  private val files_list = qs(".files", side_menu)!!

  class LoginView() {
    val btn_login = qs("input[type='button'].login")!!
    val btn_register = qs("input[type='button'].register")!!
    val input_username = qs("input[type='text'].username")!! as HTMLInputElement
    val input_password = qs("input[type='password'].password")!! as HTMLInputElement
  }

  inner class Renderer() {
    fun overview() {
      content.innerHTML = template.overview()
    }

    fun doc(doc_content: String) {
      content.innerHTML = doc_content
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
    fun logedOut() {
      content.innerHTML = template.logedOut()
      files_list.innerHTML = "Log in to see files"
      loginView = LoginView()
    }
  }

  inner class Binder {
    fun toggleMenu(handler: () -> Unit) {
      menu_toggle.addEventListener("click", { _ -> handler() })
    }
    fun handleError(e: Error) {
      window.alert(e.msg)
    }
    fun login(handler: (String, String) -> Unit) {
      loginView!!.apply { btn_login.addEventListener("click", { _ -> handler(input_username.value, input_password.value) }) }
    }
    fun register(handler: (String, String) -> Unit) {
      loginView!!.apply { btn_register.addEventListener("click", { _ -> handler(input_username.value, input_password.value) }) }
    }
  }
}
