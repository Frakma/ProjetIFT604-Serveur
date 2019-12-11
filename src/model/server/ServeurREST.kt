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
import io.ktor.request.uri
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
import org.json.JSONArray
import org.json.JSONObject
import projetift604.model.place.Location
import projetift604.model.place.Place
import projetift604.model.place.Place_Info
import projetift604.model.place.Place_Page
import projetift604.server.UserRepository
import projetift604.server.fb.PlaceRepository
import projetift604.server.fb.ServeurFBProxy
import projetift604.user.SearchCall
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
                System.out.println("empty acess token :${e}")
                val user = initUser(e.userId!!)
                val searchCall = e.searchCall

                val LOCK = Object()
                synchronized(LOCK) {
                    sleep(e.retry_in)
                    LOCK.notify()
                }
                val placesCall = search(e.data, e.resumeAt, user, searchCall!!)

                val response = Response(status = "OK", data = placesCall.toString())
                call.respond(formatResponse(searchCall, response, user))
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
                val params = call.parameters
                val searchCall = SearchCall(call.request.uri, params)
                val session = call.sessions.get<LoginSession>()
                val user = initUser(session!!.id)

                call.sessions.set(LoginSession(user.id))

                val response = Response(status = "OK", data = user.id)
                call.respond(formatResponse(searchCall, response, user))
            }
            get("/{userId?}") {
                val params = call.receiveParameters()
                val searchCall = SearchCall(call.request.uri, params)
                val session = call.sessions.get<LoginSession>()

                val userId = params.get("userId") ?: call.sessions.get<LoginSession>()!!.id ?: ""
                val user = findUser(userId)
                val status = if (user !== null) "OK" else "NOT OK"
                if (user != null) {
                    call.sessions.set(LoginSession(user.id))
                }

                val response = Response(status = status, data = user.toString())
                call.respond(formatResponse(searchCall, response, user))
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
                    val params = call.parameters
                    val searchCall = SearchCall(call.request.uri, params)
                    val session = call.sessions.get<LoginSession>()
                    val user = initUser(session!!.id)

                    val placesCall = search(null, resumeAt = 0, user = user, searchCall = searchCall)

                    val response = Response(status = "OK", data = placesCall.toString())
                    call.respond(formatResponse(searchCall, response, user))
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


    private fun formatResponse(call: SearchCall, response: Response, user: User?): Response {
        user!!.addLastResearch(call, response)
        System.out.println(
            "-------------" + "\n" +
                    "call: ${call}" + "\n" +
                    "resp: ${response}" + "\n" +
                    "user: ${user}" + "\n" +
                    "-------------" + "\n"
        )
        return response
    }

    val repository = UserRepository()
    fun findUser(userId: String): User? {
        return repository.get(userId)
    }

    fun initUser(userId: String = UUID.randomUUID().toString()): User {
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

    val placeRepository = PlaceRepository()
    fun search(data: JSONObject?, resumeAt: Int? = -1, user: User, searchCall: SearchCall): JSONObject? {
        when (resumeAt) {
            0 -> {
                return search(data, 1, user, searchCall)
            }
            1 -> {
                val places = ServeurFBProxy.searchForPlaces(
                    center = "45.3865903,-71.9261441",
                    distance = "3000",
                    q = "bar",
                    fields = "id,name",
                    limit = "10",
                    u = user,
                    s = searchCall
                )
                return search(places, 2, user, searchCall)
            }
            2 -> {
                System.out.println(data)
                if (data!!.has("data")) {
                    val items = data.getJSONArray("data")
                    for (i in 0 until items.length()) {
                        val place = items.getJSONObject(i)
                        val name = place.getString("name")
                        val id = place.getString("id")
                        val structuredPlace = Place(id, name)
                        val fieldsWanted = "page,location"

                        val place_info = ServeurFBProxy.searchForPlaceInfo(
                            placeId = id,
                            fields = fieldsWanted,
                            d = data,
                            u = user,
                            s = searchCall
                        )!!
                        place.put("place_info", place_info)
                        var location: JSONObject = JSONObject("{}")
                        if (place_info.has("location")) {
                            location = JSONObject(place_info.get("location"))
                        }

                        var page: JSONObject = JSONObject("{}")
                        var pageId: String = ""
                        if (place_info.has("page")) {
                            page = JSONObject(place_info.get("page"))
                            if (page.has("id")) {
                                pageId = page.getString("id")
                            }
                        }

                        structuredPlace.place_info = Place_Info(
                            pageId = pageId,
                            location = Location(
                                latitude = if (location.has("latitude")) location.getString("latitude") else null,
                                longitude = if (location.has("longitude")) location.getString("longitude") else null,
                                value = location
                            ),
                            page = Place_Page(
                                dunno = page.toString()
                            )
                        )
                        placeRepository.add(structuredPlace)
                    }
                    val data_ = JSONObject("{}").put("items", JSONArray(placeRepository.getAll()))
                    return search(data_, 3, user, searchCall)
                }
                return search(data, 3, user, searchCall)
            }
            else -> {
                return data
            }
        }
    }


    fun start() {
        server.start(wait = true)
    }
}


