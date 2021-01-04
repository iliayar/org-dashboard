package org.dashboard

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import kotlinx.html.*
import kotlinx.html.dom.*

import orgmode.parser.RegexOrgParser
import orgmode.parser.StringSource
import orgmode.MarkupText
import orgmode.Planning
import orgmode.OrgDocument


class Template() {
  fun loginTemplate(): String {
    return "Login form here"
  }
  fun overview(): OrgDocument {
    return RegexOrgParser(StringSource("""
* Org Dashboard

Here you can save your org documents and track
deadlines and make schedule

* DONE Headings
Begins with stars \\
You can also prepend headings with todo words(TODO, FIXME, DONE)
#+BEGIN_SRC
* Heading
* DONE Heading
#+END_SRC

* Basic markup
Very similar to /markdown/ \\
Double backslash to break line
#+BEGIN_SRC
*Bold*
/Italic/
_Underline_
+Strikeout+
[[https://iliayar.ru][Links]]
=code=

#+END_SRC

*Bold* \\
/Italic/ \\
_Underline_ \\
+Strikeout+ \\
=code= \\
[[https://iliayar.ru][Links]]

Multiline code starts with line =#+BEGIN_SRC= and ends with =#+END_SRC=

** Lists
#+BEGIN_SRC
- unordered
- lists


1. ordered
2. lists
#+END_SRC

- unordered
- lists


1. ordered
2. lists

*** Lists with checkboxes
#+BEGIN_SRC
- [ ] first thing
- [X] done thing
#+END_SRC

- [ ] first thing
- [X] done thing

* Planning
DEADLINE: <2020-11-20 Fri 13:37>

You can plan deadline or scheduling of heading \\
Write these on the next line after heading
#+BEGIN_SRC
DEADLINE: <2020-11-20 Fri 13:37>
#+END_SRC

* Tables
You can create table! \\
/Note:/ It's ok for columns be different width, works anyway
#+BEGIN_SRC
| column | another column |
|------+-------------|
| value 1 | value 2 |
| 2 | 3 |
#+END_SRC
| column | another column |
|------+-------------|
| value 1 | value 2 |
| 2 | 3 |
""")).parse() as OrgDocument
  }
  fun logedOut(): String {
    return document.create.div("login-form") {
      div("login-form container") {
        div("login-form line") {
          label() {
            +"Login: "
          }
          input() {
            type = enumValueOf("text")
            classes = setOf("username")
          }
        }
        div("login-form line") {
          label() {
            +"Password: "
          }
          input() {
            type = enumValueOf("password")
            classes = setOf("password")
          }
        }
        div("login-form line submit") {
          input() {
            type = enumValueOf("button")
            classes = setOf("submit login")
            value = "Login"
          }
          input() {
            type = enumValueOf("button")
            classes = setOf("submit register")
            value = "Register"
          }
        }
      }
    }.outerHTML
  }
  fun showDocuments(docs: List<Document>): String {
    return document.create.div {
      for(doc in docs) {
        div("document-entry") {
          attributes["name"] = doc.id!!.toString()
          +doc.name
        }
      }
    }.innerHTML
  }
  fun userInfo(user: User): String {
    return document.create.div("user-info") {
      label() {
        +user.name
      }
      input() {
        type = enumValueOf("button")
        classes = setOf("submit logout")
        value = "Log out"
      }
    }.innerHTML
  }
  fun authContent(): String {
    return document.create.div() {
      div("document") {
      }
      div("tools") {
        div("tools-btns") {
          div("tool-btn-calendar tool-btn") {
            +"Agenda"
          }
          div("tool-btn-editor tool-btn") {
            +"Editor"
          }
        }
        div("calendar") {

        }
        div("editor tool-hidden") {
          attributes["contenteditable"] = ""
        }
      }
    }.innerHTML
  }
  fun calendar(entries: List<Pair<MarkupText, Planning>>): String {
    return document.create.div() {
      for((text, date) in entries) {
        div() {
          unsafe {
            +text.toHtml()
            +" "
            +date.toHtml()
          }
        }
      }
    }.innerHTML
  }
}
