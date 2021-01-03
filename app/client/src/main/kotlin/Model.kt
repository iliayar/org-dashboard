package org.dashboard

import org.w3c.*
import org.w3c.fetch.*
import org.w3c.dom.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.browser.localStorage

import orgmode.parser.RegexOrgParser
import orgmode.parser.StringSource
import orgmode.OrgDocument

class Model() {

  fun getDoc(name: String): String {
    return """
* Test Doc
Test content
    """
  }
  fun getUser(error_cb: (Error) -> Unit, cb: (User) -> Unit) {
    val user: User? = localStorage["user"]?.let { Json.decodeFromString<User>(it) }
    if(user != null) {
      cb(user)
      return
    }
    parseResult<User>("/api/user", "GET", ::decodeUser, error_cb) {
      u: User ->
        localStorage["user"] = Json.encodeToString(u)
      cb(u)
    }
  }

  fun authenticate(auth: UserAuth, error_cb: (Error) -> Unit, cb: (User) -> Unit) {
    val user: User? = localStorage["user"]?.let { decodeUser(it) }
    if(user != null && user.authenticated) {
      cb(user)
      return
    }
    sendAndParseResult("/api/auth", "POST", auth, ::decodeUser, ::encodeUserAuth, error_cb) {
      u: User ->
        localStorage["user"] = Json.encodeToString(u)
        cb(u)
    }
  }

  fun register(auth: UserAuth, error_cb: (Error) -> Unit, cb: (User) -> Unit) {
    sendAndParseResult("/api/register", "POST", auth, ::decodeUser, ::encodeUserAuth, error_cb) {
      u: User ->
        localStorage["user"] = Json.encodeToString(u)
        cb(u)
    }
  }

  fun getDocuments(error_cb: (Error) -> Unit, cb: (List<Document>) -> Unit) {
    val docs: List<Document>? = localStorage["docs"]?.let { decodeDocuments(it) }
    if(docs != null) {
      cb(docs)
      return
    }
    parseResult("/api/document/list", "GET", ::decodeDocuments, error_cb) {
      docs: List<Document> ->
        localStorage["docs"] = Json.encodeToString(docs)
      cb(docs)
    }
  }

  fun logout() {
    localStorage.removeItem("user")
  }

  fun getDocument(name: String, error_cb: (Error) -> Unit, cb: (Document) -> Unit) {
    getDocuments(error_cb) {
      docs ->
        var found: Boolean = false
        for(doc in docs) {
          if(doc.name == name) { // :FIXME: Search by id
            found = true
            cb(doc)
          }
        }
        if(!found) {
          error_cb(Error("Document not found"))
        }
    }
  }

  fun saveDocument(doc: Document, error_cb: (Error) -> Unit, cb: () -> Unit) {
    var docs: List<Document>? = localStorage["docs"]?.let { decodeDocuments(it) }
    if(docs != null) {
      var found: Boolean = false
      for(d in docs) {
        if(d.name == doc.name) {
          val org_doc: OrgDocument = RegexOrgParser(StringSource(doc.content)).parse() as OrgDocument
          d.content = doc.content
          d.name = org_doc.title ?: "Untitled"
          found = true
          break
        }
      }
      if(!found) {
        docs += doc
      }
      localStorage["docs"] = Json.encodeToString(docs)
    }
    sendAndParseResult("/api/document", "POST", doc, {s -> s}, ::encodeDocument, error_cb) {
      cb()
    }
  }

  fun deleteDocument(doc: Document, error_cb: (Error) -> Unit, cb: () -> Unit) {
    var docs: List<Document>? = localStorage["docs"]?.let { decodeDocuments(it) }
    var new_docs: List<Document> = listOf()
    if(docs != null) {
      for(d in docs) {
        if(d.name != doc.name) {
          new_docs += d
        }
      }
      localStorage["docs"] = Json.encodeToString(new_docs)
    }
    sendAndParseResult("/api/document", "DELETE", doc, {s -> s}, ::encodeDocument, error_cb) {
      cb()
    }
  }

  private fun decodeUser(s: String): User = Json.decodeFromString<User>(s)
  private fun decodeDocuments(s: String): List<Document> = Json.decodeFromString<List<Document>>(s)
  private fun encodeUser(u: User): String = Json.encodeToString(u)
  private fun encodeUserAuth(u: UserAuth): String = Json.encodeToString(u)
  private fun encodeDocument(d: Document): String = Json.encodeToString(d)
  private fun decodeDocument(s: String): Document = Json.decodeFromString<Document>(s);
}
