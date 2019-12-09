package serveur

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import io.ktor.util.hex
import java.lang.reflect.Modifier
import java.text.DateFormat
import kotlin.collections.set


class ServeurREST {
    val server = embeddedServer(Netty, port = 8080) {
        install(DefaultHeaders)
        install(CORS) {
            anyHost()
            allowCredentials = true
        }
        install(StatusPages) {
            exception<HttpRedirectException> { e ->
                call.respondRedirect(e.location, permanent = e.permanent)
            }
            exception<Throwable> { e ->
                call.respondText(e.localizedMessage, ContentType.Text.Plain, HttpStatusCode.InternalServerError)
            }
            statusFile(HttpStatusCode.NotFound, HttpStatusCode.Unauthorized, filePattern = "error.#.html")
        }
        ///*
        install(ContentNegotiation) {
            /*serialization(
                contentType = ContentType.Application.Json,
                json = Json(
                    DefaultJsonConfiguration.copy(
                        prettyPrint = true
                    )
                )
            )*/

            gson {
                setPrettyPrinting()
                disableHtmlEscaping()
                enableComplexMapKeySerialization()
                serializeNulls()
                serializeSpecialFloatingPointValues()
                setDateFormat(DateFormat.DEFAULT)
                generateNonExecutableJson()
                setLenient()
                setVersion(0.0)
                excludeFieldsWithModifiers(Modifier.TRANSIENT)
                register(ContentType.Application.Json, GsonConverter(GsonBuilder().apply {
                    // ...
                }.create()))
            }
        }
        //
        install(CallLogging)
        install(XForwardedHeaderSupport)
/*
        install(HttpsRedirect) {
            sslPort = 443
        }
*/

        install(Sessions) {
            cookie<LoginSession>("SESSION") {
                val secretSignKey = hex("000102030405060708090a0b0c0d0e0f")
                transform(SessionTransportTransformerMessageAuthentication(secretSignKey, "HmacSHA256"))
                cookie.extensions["SameSite"] = "lax"
                cookie.path = "/"
            }
        }

        routing {
            get("/") {
                call.respond(Response(status = "|..|"))
            }
            route("/user") {
                get("") {
                    call.respond(Response(status = "O__o"))
                }
                get("co") {
                    val session = call.sessions.get<LoginSession>() ?: LoginSession(userId = "0")
                    call.sessions.set<LoginSession>(session)
                    call.respond(Response(status = "OK", data = "route = '/'"))
                }
                get("{userId}") {
                    val params = call.receiveParameters()
                    val userId = params.get("userId") ?: ""
                    val status =
                        if (call.sessions.get<LoginSession>()!!.equals(LoginSession(userId))) "OK" else "NOT OK"
                    call.respond(Response(status = status, data = "route = '/user/${userId}'"))
                }
            }
            route("/callback") {
                get("{args...}") {
                    val params = call.receiveParameters()
                    call.respond(Response(status = "OK", data = "route = '/callback/$params'"))
                }
            }
            route("/search") {
                post("") {
                    redirect("/", permanent = false)
                    call.respond(Response(status = "OK"))
                }
            }
        }

        /*
        handleRequest(HttpMethod.Get, "/", {
            addHeader(HttpHeaders.XForwardedProto, "https")
        }).let { call ->
            assertEquals(HttpStatusCode.OK, call.response.status())
        }
        */
    }

    fun resp(rep: Response): String {
        return Gson().toJson(rep)
    }

    data class Response(val status: String, val data: String = "")
    data class LoginSession(val userId: String)

    class HttpRedirectException(val location: String, val permanent: Boolean = false) : RuntimeException()

    fun redirect(location: String, permanent: Boolean = false): Nothing =
        throw HttpRedirectException(location, permanent)

    fun StatusPages.Configuration.registerRedirections() {
        exception<HttpRedirectException> { cause ->
            call.respondRedirect(cause.location, cause.permanent)
        }
    }

    fun start() {
        server.start(wait = true)
    }
}

