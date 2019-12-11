package serveur


import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.CachingOptions
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
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONObject
import projetift604.server.Repository
import projetift604.server.UserRepository
import projetift604.server.fb.ServeurFBProxy
import projetift604.user.User
import java.lang.Thread.sleep
import java.lang.reflect.Modifier
import java.text.DateFormat
import java.util.*
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
            exception<ServeurFBProxy.Companion.FBUnauthorizedRequestException> { e ->
                call.respond(Response(status = "Unauthorized"))
            }
            exception<ServeurFBProxy.Companion.EmptyAccessTokenException> { e ->
                val LOCK = Object()
                synchronized(LOCK) {
                    sleep(e.retry_in)
                    LOCK.notify()
                }
                val placesCall = search(e.data, e.resumeAt)
                call.respond(Response(status = "OK", data = placesCall.toString()))
            }
            exception<Throwable> { e ->
                call.respondText(e.localizedMessage, ContentType.Text.Plain, HttpStatusCode.InternalServerError)
            }
            //statusFile(HttpStatusCode.NotFound, HttpStatusCode.Unauthorized, filePattern = "error.#.html")
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
                setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                generateNonExecutableJson()
                setLenient()
                setVersion(0.0)
                excludeFieldsWithModifiers(Modifier.TRANSIENT)
                register(ContentType.Application.Json, GsonConverter(GsonBuilder().apply {}.create()))
            }
        }
        //
        install(CallLogging)
        install(XForwardedHeaderSupport)

        install(CachingHeaders) {
            options { outgoingContent ->
                when (outgoingContent.contentType?.withoutParameters()) {
                    ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
                    else -> null
                }
            }
        }
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
                val session = call.sessions.get<LoginSession>()
                val user = initUser(session?.id ?: UUID.randomUUID().toString())
                call.sessions.set(LoginSession(user.id))
                call.respond(Response(status = "OK", data = user.id))
            }
            get("/{userId?}") {
                val params = call.parameters
                val userId = params.get("userId") ?: call.sessions.get<LoginSession>()!!.id ?: ""
                val user = findUser(userId)
                val status = if (user !== null) "OK" else "NOT OK"
                call.respond(Response(status = status, data = user.toString()))
            }
            route("/callback") {
                get("{args...}") {
                    val params = call.receiveParameters()
                    call.respond(Response(status = "OK", data = "route = '/callback/$params'"))
                }
                get("") { call.respond(Response(status = "OK")) }
            }
            route("/search") {
                get("") {
                    redirect("/", permanent = false)
                    //call.respond(Response(status = "OK"))
                }
                post("") {
                    val placesCall = search(null)
                    call.respond(Response(status = "OK", data = placesCall.toString()))
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

    val repository: Repository<String, User> = UserRepository()
    fun findUser(userId: String): User? {
        return repository.get(userId)
    }

    fun initUser(userId: String): User {
        return repository.get(userId) ?: repository.add(User(userId))
    }

    fun resp(rep: Response): String {
        return Gson().toJson(rep)
    }

    data class Response(val status: String, val data: String = "")
    data class LoginSession(val id: String)

    class HttpRedirectException(val location: String, val permanent: Boolean = false) : RuntimeException()

    fun redirect(location: String, permanent: Boolean = false): Nothing =
        throw HttpRedirectException(location, permanent)

    fun search(data: JSONObject?, resumeAt: Int = 0): JSONObject? {
        when (resumeAt) {
            0 -> {
                return search(data, 1)
            }
            1 -> {
                val places = ServeurFBProxy.searchForPlaces(
                    center = "45.3865903,-71.9261441",
                    distance = "3000",
                    q = "bar",
                    fields = "id,name",
                    limit = "10"
                )
                val code = places.code()
                var body = places.body()
                when (code) {
                    190 -> ServeurFBProxy.resetAccess_token()
                    200 -> System.out.println(places)
                    400 -> body = ResponseBody.create(
                        MediaType.parse("application/json"),
                        "{}"
                    )//ServeurFBProxy.FBunauthorizedRequest()
                }
                val json = JSONObject(body)
                return search(json, 2)
            }
            2 -> {
                if (data!!.has("data")) {
                    val items = data.getJSONArray("data")
                    for (i in 0 until items.length()) {
                        {
                            var place = items.getJSONObject(i)
                            val name = place.getString("name")
                            val id = place.getString("id")
                            val fieldsWanted = ""

                            val place_info = ServeurFBProxy.searchForPlaceInfo(
                                placeId = id,
                                fields = fieldsWanted
                            )
                            var body = place_info.body()
                            val json = JSONObject(body)
                            //TODO
                            search(json, 3)
                        }
                    }
                }
                return data
            }
            else -> {
                return null
            }
        }
    }


    fun start() {
        server.start(wait = true)
    }
}


