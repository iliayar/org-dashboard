package org.dashboard.server

import kotlinx.serialization.*

@Serializable
class Document(
  val name: String,
  val owner: User,
  val content: String,
  val shared: Boolean = false
) {
}

@Serializable
class User(
  val name: String,
  val authenticated: Boolean = false,
  val documents: List<Document> = listOf()
) {
}

@Serializable
class Error(
  val msg: String
)
