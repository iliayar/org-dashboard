import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import java.io.File
import java.util.*
import orgmode.parser.RegexOrgParser

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
                        href = "/css/colors.css"
                        rel = "stylesheet"
                    }

		    link {
                        href = "https://use.fontawesome.com/releases/v5.8.1/css/all.css"
                        rel = "stylesheet"
                    }


                    link {
                        href = "/css/dashboard.css"
                        rel = "stylesheet"
                    }

                    script {
                        src = "/client.js"
                    }
                }

                body {
		    header() {
			div("container") {
			    i("fas fa-bars") { id = "menu-toggle-btn" }
			    h1() { +"Org Dashboard" }
			    div()
			}
		    }
		    div("side-menu") {
			+"Files will be here"
		    }
                }
            }
        }

        static("/") {
            resources("/")
        }

    }
}
