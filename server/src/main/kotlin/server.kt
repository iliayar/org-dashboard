package org.dashboard.server

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import kotlinx.html.*
import java.io.File
import java.util.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import io.ktor.serialization.*
import io.ktor.features.*
import java.security.MessageDigest


fun Application.main() {
  routing {
      get("/") {
        call.respondHtml {
          head {
            title {
              +"Org Dashboard"
            }

            link {
              href = "/favicon.png"
              rel = "icon"
            }

            link {
              href = "https://use.fontawesome.com/releases/v5.8.1/css/all.css"
              rel = "stylesheet"
            }

            link {
              href = "css/colors.css"
              rel = "stylesheet"
            }

            link {
              href = "css/dashboard.css"
              rel = "stylesheet"
            }

            script {
              src = "/client.js"
            }
          }
        }
      }

    static("/") {
      resources("/")
    }
    install(ContentNegotiation) {
      json()
    }
    route("/api") {
      // :FIXME: On this stage we fully trust client Later User will
      // be fetched from session cookies or token
      post("/auth") {
        val auth = call.receive<UserAuth>();
        // :TODO: Gettign real users
        if(auth.name == "admin" && auth.password == sha256("admin")) {
          call.respond(HttpStatusCode.OK, User(auth.name, true))
        } else {
          call.respond(HttpStatusCode(420, "Not authenticated"), Error("Invalid username of password"))
        }
      }
      post("/register") {
        val auth = call.receive<UserAuth>();
        // :TODO: adding user
        call.respond(HttpStatusCode.OK, User(auth.name, true));
      }
        get("/documents") {
          val user = User("admin", true)
          // :TODO: Getting user's real documents
          if(user.authenticated) {
            call.respond(HttpStatusCode.OK, listOf(Document("test", user.name, "* Test Document"), Document("Another test", user.name, "* Another test Document")))
          } else {
            call.respond(HttpStatusCode(420, "Not authenticated"), Error("Not authenticated, cannot fetch documents"))
          }
        }
        get("/user") {
          // :TODO: Getting current user or Error in case not authenticated
          call.respond(User("admin", false));
        }
    }
  }
}

fun sha256(text: String): String {
  val md = MessageDigest.getInstance("SHA-256")
  val hash = md.digest(text.toByteArray())
  return hash.fold("") { str, it -> str + "%02x".format(it) }
}
