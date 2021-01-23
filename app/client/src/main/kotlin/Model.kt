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
  fun getUser(error_cb: (Error) -> Unit, cb: (User?) -> Unit) {
    val user: User? = localStorage["user"]?.let { Json.decodeFromString<User>(it) }
    cb(user)
  }

  fun authenticate(auth: UserAuth, error_cb: (Error) -> Unit, cb: (User) -> Unit) {
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
    parseResult("/api/document/list?token=${getToken()}", "GET", ::decodeDocuments, error_cb) {
      docs: List<Document> -> cb(docs)
    }
  }

  fun logout() {
    localStorage.removeItem("user")
  }

  fun getDocument(id: Int, error_cb: (Error) -> Unit, cb: (Document) -> Unit) {
    getDocuments(error_cb) {
      docs ->
        var found: Boolean = false
        for(doc in docs) {
          if(doc.id == id) {
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
    val org_doc: OrgDocument = RegexOrgParser(StringSource(doc.content)).parse() as OrgDocument
    doc.name = org_doc.title ?: "Untitled"
    sendAndParseResult("/api/document?token=${getToken()}", "POST", doc, {s -> s}, ::encodeDocument, error_cb) {
      cb()
    }
  }

  fun deleteDocument(doc: Document, error_cb: (Error) -> Unit, cb: () -> Unit) {
    sendAndParseResult("/api/document?token=${getToken()}", "DELETE", doc, {s -> s}, ::encodeDocument, error_cb) {
      cb()
    }
  }

  private fun getToken(): String {
    return decodeUser(localStorage["user"]!!).token!!
  }

  private fun decodeUser(s: String): User = Json.decodeFromString<User>(s)
  private fun decodeDocuments(s: String): List<Document> = Json.decodeFromString<List<Document>>(s)
  private fun encodeUser(u: User): String = Json.encodeToString(u)
  private fun encodeUserAuth(u: UserAuth): String = Json.encodeToString(u)
  private fun encodeDocument(d: Document): String = Json.encodeToString(d)
  private fun decodeDocument(s: String): Document = Json.decodeFromString<Document>(s);
}
