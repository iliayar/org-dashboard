package org.dashboard

import java.security.MessageDigest

val charPool = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUWXYZ"

fun generateToken(length: Int = 64): String {
  return (1..length)
    .map { i -> kotlin.random.Random.nextInt(0, charPool.length) }
    .map { i -> charPool[i] }
    .joinToString("")
}


fun sha256(text: String): String {
  val md = MessageDigest.getInstance("SHA-256")
  val hash = md.digest(text.toByteArray())
  return hash.fold("") { str, it -> str + "%02x".format(it) }
}
