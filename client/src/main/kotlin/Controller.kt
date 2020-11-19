package org.dashboard

import kotlinx.browser.window

class Controller(private val model: Model, private val view: View) {
  // TODO: Create methods

  init {
    view.bind.toggleMenu { toggleMenu() }
  }

  fun setView(path: String?, hash: String?, logedIn: Boolean) {
    if(!logedIn) {
      view.render.logedOut();
    } else {
      val doc = path?.split("/")?.elementAtOrElse(1, { _ -> "" }) ?: ""
      updateDoc(doc);
      updateFiles();
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
}
