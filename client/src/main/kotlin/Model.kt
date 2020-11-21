package org.dashboard

import org.w3c.*
import org.w3c.fetch.*
import org.w3c.dom.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.browser.localStorage

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
    }
    parseResult("/api/documents", "GET", ::decodeDocuments, error_cb) {
      docs: List<Document> ->
        localStorage["docs"] = Json.encodeToString(docs)
      cb(docs)
    }
  }

  fun logout() {
    localStorage.removeItem("user")
  }

  private fun decodeUser(s: String): User = Json.decodeFromString<User>(s)
  private fun decodeDocuments(s: String): List<Document> = Json.decodeFromString<List<Document>>(s)
  private fun encodeUser(u: User): String = Json.encodeToString(u)
  private fun encodeUserAuth(u: UserAuth): String = Json.encodeToString(u)
}
