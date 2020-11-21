package org.dashboard.server

import kotlinx.serialization.*

// :FIXME: Make separate class for Document content
@Serializable
class Document(
  val name: String,
  val user: String,
  val content: String,
  val shared: Boolean = false
) {
}

@Serializable
class User(
  val name: String,
  var authenticated: Boolean = false,
  val documents: List<Document> = listOf()
) {
}

@Serializable
class UserAuth(
  val name: String,
  val password: String,
) {
}

@Serializable
class Error(
  val msg: String
)
