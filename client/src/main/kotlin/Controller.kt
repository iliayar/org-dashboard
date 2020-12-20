package org.dashboard

import kotlinx.browser.window
import crypto.createHash

import orgmode.parser.RegexOrgParser
import orgmode.parser.StringSource
import orgmode.OrgDocument

class Controller(private val model: Model, private val view: View) {

  var current_doc: Document? = null

  init {
    view.bind.toggleMenu(::toggleMenu)
    view.bind.save(::saveDocument)
    view.bind.new(::newDocument)
  }

  fun setViewCallback(user: User, path: String? , hash: String?) {
    if(!user.authenticated) {
      view.render.logedOut()
      view.bind.login(::login)
      view.bind.register(::register)
    } else {
      val doc = path?.split("/")?.elementAtOrElse(1, { _ -> "" }) ?: ""
      view.render.logedIn(user)
      updateDocuments()
      updateDoc(doc)
      view.bind.logout(::logout)
      view.bind.orgEditorEdit(::orgEdit)
      view.bind.enableCalendar(view.render::enableCalendar)
      view.bind.enableEditor(view.render::enableEditor)
    }
  }

  fun setView(path: String?, hash: String?) {
    model.getUser(::handleError) { user -> setViewCallback(user, path, hash) }
  }

  private fun updateDoc(name: String) {
    if(name == "") {
      view.render.overview()
    } else {
      openDocument(name)
    }
  }

  private fun updateDocuments() {
    model.getDocuments(::handleError) {
      docs ->
        view.render.updateDocuments(docs)
        view.bind.openDocument(::openDocument)
    }
  }

  private fun toggleMenu() {
    view.render.toggleMenu()
  }

  private fun login(username: String, password: String) {
    model.authenticate(UserAuth(username, sha256(password)), ::handleError) {
      user ->
        if(user.authenticated) {
          setViewCallback(user, null, null)
        }
    }
  }
  private fun register(username: String, password: String) {
    model.register(UserAuth(username, sha256(password)), ::handleError) {
      user ->
        if(user.authenticated) {
          setViewCallback(user, null, null)
        }
    }
  }

  private fun openDocument(name: String) {
    model.getDocument(name, ::handleError) {
      doc -> view.render.updateDocument(RegexOrgParser(StringSource(doc.content)).parse() as OrgDocument)
      current_doc = doc
    }
  }

  private fun newDocument() {
    model.getUser(::handleError) {
      user -> model.saveDocument(Document("Untitled", user.name, "#+TITLE: Untitled"), ::handleError) {
        updateDocuments()
      }
    }
  }

  private fun saveDocument() {
    if(current_doc != null) {
      model.saveDocument(current_doc!!, ::handleError) {
        updateDocuments()
      }
    }
  }

  private fun logout() {
    model.logout()
    setView(null, null)
  }

  private fun orgEdit(content: String) {
    if(current_doc != null) {
      current_doc!!.content = content
    }
    view.render.editDocument(RegexOrgParser(StringSource(content)).parse() as OrgDocument)
  }

  private fun handleError(e: Error) = view.bind.handleError(e)
  private fun sha256(d: String): String {
    return createHash("sha256").run {
      update(d)
      digest("hex")
    }
  }
}
