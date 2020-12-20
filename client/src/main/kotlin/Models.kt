package org.dashboard

import kotlinx.serialization.*

// :FIXME: Make separate class for Document content
@Serializable
data class Document(
  var name: String,
  val user: String,
  var content: String,
  val shared: Boolean = false
) {
}

@Serializable
data class User(
  val name: String,
  var authenticated: Boolean = false,
  val documents: List<Document> = listOf()
) {
}

@Serializable
data class UserAuth(
  val name: String,
  val password: String,
) {
}

@Serializable
data class Error(
  val msg: String
)
