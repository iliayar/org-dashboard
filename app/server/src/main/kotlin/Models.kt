package org.dashboard

import kotlinx.serialization.*

// :FIXME: Make separate class for Document content
@Serializable
class Document(
  val id: Int?,
  val name: String,
  var user: Int?,
  val content: String,
) {
}

@Serializable
class User(
  val name: String,
  val token: String? = null
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
