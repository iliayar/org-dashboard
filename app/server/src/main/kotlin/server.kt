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

import org.dashboard.*
import org.dashboard.database.*

fun Application.main() {
  var db = DashboardDB()
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
      post("/auth") {
        val auth = call.receive<UserAuth>();
        val userId: Int? = db.checkUser(auth.name, auth.password)
        if(userId != null) {
          call.respond(HttpStatusCode.OK, User(auth.name, token = db.authorize(userId)))
        } else {
          call.respond(HttpStatusCode(420, "Not authenticated"), Error("Invalid username of password"))
        }
      }
      post("/register") {
        val auth = call.receive<UserAuth>();
        val userId: Int? = db.addUser(auth.name, auth.password)
        if(userId != null) {
          call.respond(HttpStatusCode.OK, User(auth.name, token = db.authorize(userId)));
        } else {
          call.respond(HttpStatusCode(420, "Register failed"), Error("Username with such name already exists"))
        }
      }
        get("/document/list") {
          val token = call.parameters["token"]!!
          val userId = db.getSession(token)
          if(userId != null) {
            call.respond(HttpStatusCode.OK, db.getDocuments(userId))
          } else {
            call.respond(HttpStatusCode(420, "Not authenticated"), Error("Not authenticated, cannot fetch documents"))
          }
        }
      post("/document") {
        val token = call.parameters["token"]!!
        val doc = call.receive<Document>();
        val userId = db.getSession(token)
        if(doc.id == null) {
          val docId = db.createDocument(Document(null, doc.name, userId!!, doc.content))
          call.respond(HttpStatusCode.OK);
        } else {
          if(userId != db.getDocument(doc.id).user) {
            call.respond(HttpStatusCode(420, "Not authorized"), Error("You are not authorized to edit this document"))
          } else {
            db.saveDocument(doc)
            call.respond(HttpStatusCode.OK);
          }
        }
      }
      delete("/document") {
        val token = call.parameters["token"]!!
        val doc = call.receive<Document>();
        val userId = db.getSession(token)
        if(doc.id == null) {
            call.respond(HttpStatusCode(404, "Not found"), Error("Document not found"))
        } else {
          if(db.getDocument(doc.id).user != userId) {
            call.respond(HttpStatusCode(420, "Not authorized"), Error("You are not authorized to delete this document"))
          }
          db.deleteDocument(doc)
          call.respond(HttpStatusCode.OK);
        }
      }
    }
  }
}
