package org.dashboard

import org.w3c.dom.*
import kotlinx.browser.document
import kotlinx.browser.window

import orgmode.OrgDocument

open class View(private val template: Template) {
  val render = Renderer()
  val bind = Binder()
  var loginView: LoginView? = null
  var auth: Authenticated? = null
  var docs: DocumentsView? = null

  private val body = document.body as HTMLElement
  private val menu_toggle = qs(".menu-toggle-btn")!!
  private val side_menu = qs(".side-menu")!!
  private val content = qs(".content")!!
  private val files_list = qs(".files", side_menu)!!
  private val user_info = qs("header .user-info")!!
  private val save = qs(".save-btn")!!
  private val new = qs(".new-btn")!!

  inner class LoginView() {
    val btn_login = qs("input[type='button'].login")!!
    val btn_register = qs("input[type='button'].register")!!
    val input_username = qs("input[type='text'].username")!! as HTMLInputElement
    val input_password = qs("input[type='password'].password")!! as HTMLInputElement
  }

  inner class Authenticated() {
    val btn_logout = qs("input[type='button'].logout")!!
    val document = qs(".content .document")!! as HTMLElement
    val editor = qs(".tools .editor")!! as HTMLElement
    val calendar = qs(".tools .calendar")!! as HTMLElement
    val editor_btn = qs(".tool-btn-editor")!! as HTMLElement
    val calendar_btn = qs(".tool-btn-calendar")!! as HTMLElement
  }

  inner class DocumentsView() {
    val list = qsa(".document-entry")
  }

  inner class Renderer() {
    fun overview() {
       updateDocument(template.overview())
    }

    fun updateDocument(doc: OrgDocument) {
      auth!!.document.innerHTML = doc.toHtml()
      auth!!.editor.textContent = doc.toString()
      auth!!.calendar.innerHTML = template.calendar(doc.getPlanning())
    }
    fun editDocument(doc: OrgDocument) {
      auth!!.document.innerHTML = doc.toHtml()
      auth!!.calendar.innerHTML = template.calendar(doc.getPlanning())
    }
    fun toggleMenu() {
      if(side_menu.classList.contains("active")) {
        side_menu.classList.remove("active");
      } else {
        side_menu.classList.add("active");
      }
    }
    fun enableCalendar() {
      if(auth!!.calendar.classList.contains("tool-hidden")) {
        auth!!.calendar.classList.remove("tool-hidden")
        auth!!.editor.classList.add("tool-hidden")
      }
    }
    fun enableEditor() {
      if(auth!!.editor.classList.contains("tool-hidden")) {
        auth!!.editor.classList.remove("tool-hidden")
        auth!!.calendar.classList.add("tool-hidden")
      }
    }
    fun updateDocuments(documents: List<Document>) {
      files_list.innerHTML = template.showDocuments(documents)
      docs = DocumentsView()
    }
    fun logedOut() {
      content.innerHTML = template.logedOut()
      files_list.innerHTML = "Log in to see files"
      user_info.innerHTML = "Not authenticated"
      loginView = LoginView()
    }
    fun logedIn(user: User) {
      user_info.innerHTML = template.userInfo(user)
      content.innerHTML = template.authContent()
      auth = Authenticated()
    }
  }

  inner class Binder {
    fun new(handler: () -> Unit) {
      new.addEventListener("click", { _ -> handler()})
    }
    fun save(handler: () -> Unit) {
      save.addEventListener("click", { _ -> handler()})
    }
    fun toggleMenu(handler: () -> Unit) {
      menu_toggle.addEventListener("click", { _ -> handler() })
    }
    fun enableEditor(handler: () -> Unit) {
      auth!!.editor_btn.addEventListener("click", { _ -> handler()})
    }
    fun enableCalendar(handler: () -> Unit) {
      auth!!.calendar_btn.addEventListener("click", { _ -> handler()})
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
    fun logout(handler: () -> Unit) {
      auth!!.btn_logout.addEventListener("click", { _ -> handler() })
    }
    fun openDocument(handler: (String) -> Unit) {
      for(doc in docs!!.list) {
        doc.addEventListener("click", {e -> handler((e.target as HTMLElement).attributes["name"]!!.value)})
      }
    }
    fun orgEditorEdit(handler: (String) -> Unit) {
      auth!!.editor.addEventListener("input", {_ -> handler(auth!!.editor.innerText) })
    }
  }
}
