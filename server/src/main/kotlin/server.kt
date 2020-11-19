import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import java.io.File
import java.util.*

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

  }
}
