package org.dashboard

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.ParentNode
import org.w3c.fetch.*
import kotlinx.browser.*
import kotlin.js.json

fun qs(selector: String, scope: Element? = null) = node(scope).querySelector(selector) as HTMLElement?


private fun node(scope: Element?): ParentNode = scope ?: document

fun <R> parseResult(url: String,
                    method: String,
                    fromJson: (String) -> R,
                    error_cb: (Error) -> Unit,
                    cb: (R) -> Unit) {
  window.fetch(url, object: RequestInit {
                 override var method: String? = method
  }).then({res -> parseResultHandler(res, fromJson, error_cb, cb)})
}

fun <T, R> sendAndParseResult(url: String,
                              method: String,
                              body: T,
                              fromJson: (String) -> R,
                              toJson: (T) -> String,
                              error_cb: (Error) -> Unit,
                              cb: (R) -> Unit) {
  window.fetch(url, object: RequestInit {
                 override var method: String? = method
                 override var body: dynamic = toJson(body)
                 override var headers: dynamic = json("Content-Type" to "application/json")

  }).then({res -> parseResultHandler(res, fromJson, error_cb, cb)})
}

fun <R> parseResultHandler(response: Response,
                           fromJson: (String) -> R,
                           error_cb: (Error) -> Unit,
                           cb: (R) -> Unit)
{
  response.text().then {
    if(response.status == 200.toShort()) {
      cb(fromJson(it))
    } else {
      error_cb(Json.decodeFromString<Error>(it))
    }
  }
}

fun sha256(text: String, cb: (String) -> Unit) {
  // crypto.subtle.digest("SHA-256", text)
}
