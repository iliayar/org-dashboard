package org.dashboard

import org.w3c.*

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
}
