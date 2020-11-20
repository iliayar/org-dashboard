package org.dashboard

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import kotlinx.html.*
import kotlinx.html.dom.*

import orgmode.parser.RegexOrgParser
import orgmode.parser.StringSource


class Template() {
  // TODO: Templates
  fun loginTemplate(): String {
    return "Login form here"
  }
  fun overview(): String {
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
""")).parse().toHtml()
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
      }
    }.outerHTML

  }
  fun showFiles(files: List<String>): String {
    return files.joinToString("</br>")
  }
}
