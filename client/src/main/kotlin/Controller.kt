package org.dashboard

import kotlinx.browser.window
import crypto.createHash

class Controller(private val model: Model, private val view: View) {
  // TODO: Create methods

  init {
    view.bind.toggleMenu(::toggleMenu)
  }

  fun setView(path: String?, hash: String?) {
    model.getUser(::handleError) {
      user ->
        if(!user.authenticated) {
          view.render.logedOut()
          view.bind.login(::login)
          view.bind.register(::register)
        } else {
          val doc = path?.split("/")?.elementAtOrElse(1, { _ -> "" }) ?: ""
          updateDoc(doc);
          updateFiles();
        }
    }
  }

  private fun updateDoc(docName: String) {
    if(docName == "") {
      view.render.overview()
    } else {
      view.render.doc(model.getDoc(docName))
    }
  }

  private fun updateFiles() {
    view.render.updateFiles(model.getFiles())
  }

  private fun toggleMenu() {
    view.render.toggleMenu()
  }

  private fun login(username: String, password: String) {
    model.authenticate(UserAuth(username, sha256(password)), ::handleError) {
      user ->
        console.log(user.toString())
    }
  }
  private fun register(username: String, password: String) {
    window.alert("Register: " + username + " " + sha256(password))
  }
  private fun handleError(e: Error) = view.bind.handleError(e)

  private fun sha256(d: String): String {
    return createHash("sha256").run {
      update(d)
      digest("hex")
    }
  }
}
