package serveur


import com.google.gson.annotations.Expose
import io.ktor.application.ApplicationCall
import io.ktor.application.MissingApplicationFeatureException
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.CachingOptions
import io.ktor.request.header
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.hex
import org.json.JSONObject
import projetift604.config.SLog
import projetift604.model.server.searchEngine.Response
import projetift604.model.server.searchEngine.SearchEngine
import projetift604.model.server.searchEngine.SearchParams
import projetift604.model.server.searchEngine.formatResponse
import projetift604.server.UserRepository
import projetift604.server.fb.ServeurFBProxy
import projetift604.user.SearchCall
import projetift604.user.User
import java.lang.Thread.sleep
import java.util.*
import kotlin.collections.set


class ServeurREST {
    @KtorExperimentalAPI
    val server = embeddedServer(Netty, port = 8080) {
        install(DefaultHeaders)
        install(ConditionalHeaders)
        install(DoubleReceive)
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
                val user: User = initUser(e.userId!!)
                val searchCall: SearchCall? = e.searchCall
                val sp: SearchParams = e.sp!!

                val LOCK = Object()
                synchronized(LOCK) {
                    sleep(e.retry_in)
                    LOCK.notify()
                }
                val placesCall = SearchEngine.search(e.data!!, e.resumeAt!!, e.sp, user, searchCall!!)!!

                val response = Response(status = "OK", data = placesCall)
                call.respond(formatResponse(searchCall, response, user))
            }
            exception<Throwable> { e ->
                call.respondText(e.localizedMessage ?: "", ContentType.Text.Plain, HttpStatusCode.InternalServerError)
            }
            //statusFile(HttpStatusCode.NotFound, HttpStatusCode.Unauthorized, filePattern = "error.#.html")
        }
        ///*
        install(Compression)
        install(ContentNegotiation) {
            /*
            jackson {
                configure(SerializationFeature.INDENT_OUTPUT, true)
                setDefaultPrettyPrinter(DefaultPrettyPrinter())
                //registerModule(JavaTimeModule())  // support java.time.* types
            }
            */

            /*//
            serialization(
                contentType = ContentType.Application.Json,
                json = Json(
                    DefaultJsonConfiguration.copy(
                        prettyPrint = true
                    )
                )
            )
            */

            gson {
                setPrettyPrinting()
                //enableComplexMapKeySerialization()
            }
            /*
            gson {
                setVersion(0.0)
                setPrettyPrinting()
                setLenient()
                //serializeNulls()
                //serializeSpecialFloatingPointValues()
                //setDateFormat(DateFormat.DEFAULT)
                setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                //excludeFieldsWithModifiers(Modifier.TRANSIENT)
                //excludeFieldsWithoutExposeAnnotation()
                //enableComplexMapKeySerialization()
                //generateNonExecutableJson()
                disableHtmlEscaping()
                register(ContentType.Application.Json, GsonConverter(GsonBuilder().apply {
                    setPrettyPrinting()
                    //setLenient()
                    serializeNulls()
                    serializeSpecialFloatingPointValues()
                    //setDateFormat(DateFormat.DEFAULT)
                    setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                    //excludeFieldsWithModifiers(Modifier.TRANSIENT)
                    //excludeFieldsWithoutExposeAnnotation()
                    enableComplexMapKeySerialization()
                    //generateNonExecutableJson()
                    //disableHtmlEscaping()
                }.create()))
            }
            */
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
                val cp = call.parameters
                val sp = SearchParams.extract(cp)
                val searchCall = SearchCall(call.request.uri, sp.toString())
                val user = takeCareOfUser(call)
                //val response = Response(status = "OK", data = JSONObject(user).put("userPref", ""))
                val response = Response(status = "OK", data = JSONObject(user))
                call.respond(formatResponse(searchCall, response, user))
            }
            get("/{userId}") {
                val cp = call.parameters
                val sc = SearchCall(call.request.uri, cp.toString())
                val session = call.sessions.get<LoginSession>()
                val userId = if (cp.contains("userId")) cp.get("userId")!! else session!!.id
                val user = findUser(userId)
                val status = if (user !== null) "OK" else "NOT OK"
                if (user != null) {
                    call.sessions.set(LoginSession(user.id))
                }
                val response = Response(status = status, data = JSONObject(user))
                call.respond(formatResponse(sc, response, user))
            }
            route("/search") {
                get("") {
                    redirect("/", permanent = false)
                    //call.respond(Response(status = "OK"))
                }
                post("") {
                    SLog.log(call.request.header("Content-Type")!!)
                    //val cp = call.receiveParameters()
                    //val cp = call.receive<String>()
                    //val sp = SearchParams.extract(cp)
                    val sp = SearchParams()
                    val searchCall = SearchCall(call.request.uri, sp.toString())
                    val user = takeCareOfUser(call)
/*
                    val callParameters = "{}"//call.receiveParameters()
                    val params = JSONObject(callParameters).toMap()
                    System.out.println(params)
                    val searchCall = SearchCall(call.request.uri, JSONObject(params))
                    System.out.println(searchCall)
                    val session = call.sessions.get<LoginSession>()
                    val user = initUser(session!!.id)

                    val sp = SearchParams.extract(params)
                    System.out.println(sp)

 */
                    val placesCall = SearchEngine.search(
                        JSONObject(sp.data),
                        resumeAt = sp.startAtPhase,
                        sp = sp,
                        user = user,
                        searchCall = searchCall
                    )!!
                    val response = Response(status = "OK", data = placesCall)
                    call.respond(formatResponse(searchCall, response, user))
                }
                put {
                    redirect("/", false)
                }
                delete {
                    redirect("/", false)
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


    val repository = UserRepository()
    private fun takeCareOfUser(call: ApplicationCall): User {
        var user = initUser()
        try {
            val session = call.sessions.get<LoginSession>()!!
            user = initUser(session.id)
        } catch (e: MissingApplicationFeatureException) {
            call.sessions.set(LoginSession(user.id))
        } finally {
            return user
        }
    }

    private fun findUser(userId: String): User? {
        return repository.get(userId)
    }

    private fun initUser(userId: String = UUID.randomUUID().toString()): User {
        return repository.get(userId) ?: repository.add(User(userId))
    }


    data class LoginSession(@Expose val id: String)

    class HttpRedirectException(val location: String, val permanent: Boolean = false) : RuntimeException()

    fun redirect(location: String, permanent: Boolean = false): Nothing =
        throw HttpRedirectException(location, permanent)

    fun start() {
        server.start(wait = true)
    }
}


