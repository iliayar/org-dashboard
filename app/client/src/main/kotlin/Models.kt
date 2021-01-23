package org.dashboard

import kotlinx.serialization.*

// :FIXME: Make separate class for Document content
@Serializable
data class Document(
  val id: Int?,
  var name: String,
  val user: Int?,
  var content: String,
) {
}

@Serializable
data class User(
  val name: String,
  val token: String? = null
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
