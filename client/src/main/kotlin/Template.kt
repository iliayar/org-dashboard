package org.dashboard

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import kotlinx.html.*
import kotlinx.html.dom.*

class Template() {
  // TODO: Templates
  fun loginTemplate(): String {
    return "Login form here"
  }
  fun overview(): String {
    return "Overview document"
  }
  fun logedOut(): String {
    return "Login form"
  }
  fun showFiles(files: List<String>): String {
    return files.joinToString("</br>")
  }
}
