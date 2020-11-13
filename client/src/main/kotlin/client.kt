import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLDivElement
import org.w3c.xhr.XMLHttpRequest

fun main() {
  window.onload = {
    var menu_toggle = document.querySelector("#menu-toggle-btn")!!
    var side_menu_is_active = false

    var toggle_menu =
      { _: Any? ->
          var side_menu = document.querySelector(".side-menu")!!
          var new_classes: Set<String>
          if(side_menu_is_active) {
            new_classes = setOf("side-menu")
            side_menu_is_active = false
          } else {
            new_classes = setOf("side-menu", "active")
            side_menu_is_active = true
          }
          side_menu.replaceWith(document.create.div {
                                  classes = new_classes
                                  +side_menu.innerHTML
          })
      }
    menu_toggle.addEventListener("click", toggle_menu)
  }
}

external fun encodeURIComponent(s: String): String
