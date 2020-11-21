package org.dashboard

import org.w3c.*
import org.w3c.fetch.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.browser.window
import kotlinx.coroutines.*

class Model(/* TODO: Data objects */) {
  // TODO: Interaction with data objects

  fun getDoc(name: String): String {
    return """
* Test Doc
Test content
    """
  }
  fun getFiles(): List<String> {
    return listOf("Files will be here")
  }
  // fun authenticate(): Boolean {

  // }
  fun getUser(error_cb: (Error) -> Unit, cb: (User) -> Unit) {
    parseResult<User>("/api/user", "GET", ::decodeUser, error_cb, cb)
  }

  fun authenticate(auth: UserAuth, error_cb: (Error) -> Unit, cb: (User) -> Unit) {
    sendAndParseResult("/api/auth", "POST", auth, ::decodeUser, ::encodeUserAuth, error_cb, cb)
  }

  private fun decodeUser(s: String): User = Json.decodeFromString<User>(s)
  private fun encodeUserAuth(u: UserAuth): String = Json.encodeToString(u)
}
