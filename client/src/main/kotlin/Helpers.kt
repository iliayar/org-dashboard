package org.dashboard

import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.ParentNode

fun qs(selector: String, scope: Element? = null) = node(scope).querySelector(selector) as HTMLElement?


private fun node(scope: Element?): ParentNode = scope ?: document
