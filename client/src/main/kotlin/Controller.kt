package org.dashboard

import kotlinx.browser.window
import crypto.createHash

import orgmode.parser.RegexOrgParser
import orgmode.parser.StringSource

class Controller(private val model: Model, private val view: View) {

  init {
    view.bind.toggleMenu(::toggleMenu)
  }

  fun setViewCallback(user: User, path: String? , hash: String?) {
    if(!user.authenticated) {
      view.render.logedOut()
      view.bind.login(::login)
      view.bind.register(::register)
    } else {
      val doc = path?.split("/")?.elementAtOrElse(1, { _ -> "" }) ?: ""
      view.render.logedIn(user)
      updateDoc(doc)
      updateDocuments()
      view.bind.logout(::logout)
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
    model.getDocumentContent(name, ::handleError) {
      content -> view.render.doc(content)
    }
  }

  private fun logout() {
    model.logout()
    setView(null, null)
  }

  private fun handleError(e: Error) = view.bind.handleError(e)
  private fun sha256(d: String): String {
    return createHash("sha256").run {
      update(d)
      digest("hex")
    }
  }
}
