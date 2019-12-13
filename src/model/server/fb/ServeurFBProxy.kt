package projetift604.server.fb

import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import projetift604.model.server.searchEngine.SearchParams
import projetift604.server.generateHashWithHmac256
import projetift604.user.SearchCall
import projetift604.user.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ServeurFBProxy {
    companion object {
        private val serveurFB = ServeurFB.create()
        var access_token_: String = ""

        /**
         * Returns access_token_
         * if this one equals "" then a new access_token is asked (async) and "" is returned
         */
        fun access_token(
            resumeAt: Int? = 0,
            data: String? = "{}",
            sp: SearchParams?,
            u: User?,
            s: SearchCall?
        ): String {
            System.out.println("token asked [${access_token_}]")
            if (!access_token_.equals("")) {
                return access_token_
            }
            ServeurFBProxy.getAccess_token()
            restart(2000, resumeAt, data, sp, u, s)
            return ""
        }

        fun getAccess_token() {
            val call = serveurFB.getAccess_token_(
                ServeurFB.app_id,
                ServeurFB.app_secret, "client_credentials"
            )
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                    System.err.println(call)
                    throw Exception(t)
                }

                override fun onResponse(
                    call: Call<ResponseBody>?,
                    response: Response<ResponseBody>
                ) {
                    val json = extractBody(response, -1, null, null, null)
                    //System.out.println("access_token responseBody: ${json}")
                    if (json.has("access_token")) {
                        access_token_ = json.getString("access_token")
                        System.out.println("new access_token: ${access_token_}")
                    }
                }
            })
        }

        /*
         * Places below
         */

        fun searchForPlaces(
            center: String,
            distance: String,
            q: String,
            fields: String,
            limit: Int,
            offset: Int,
            sp: SearchParams,
            u: User?,
            s: SearchCall?,
            resumeAt: Int? = 1
        ): JSONObject? {
            System.out.println("--searchForPlaces")
            val access_token = access_token(resumeAt, "{}", sp, u, s)
            System.out.println("--access_token: ${access_token}")
            val appsecret_proof = appsecret_proof(access_token)
            System.out.println("--appsecret_proof: ${appsecret_proof}")
            val call = serveurFB.searchForPlaces_(
                center, distance, q, fields,
                limit.toString(), access_token, appsecret_proof
            )
            System.out.println("--call: ${call}")
            val resp = call.execute()
            System.out.println("--resp: ${resp}")
            return extractBody(resp, resumeAt, sp, u, s)
        }

        fun searchForPlaceInfo(
            placeId: String,
            fields: String,
            d: JSONObject,
            sp: SearchParams,
            u: User?,
            s: SearchCall?,
            resumeAt: Int? = 2
        ): JSONObject? {
            val access_token = access_token(resumeAt, d.toString(), sp, u, s)
            val appsecret_proof = appsecret_proof(access_token)
            val call = serveurFB.searchForPlaceInfo_(placeId, fields, access_token, appsecret_proof)
            val resp = call.execute()
            return extractBody(resp, resumeAt, sp, u, s)
        }

        fun extractBody(
            resp: Response<ResponseBody>,
            resumeAt: Int? = 0,
            sp: SearchParams?,
            u: User?,
            s: SearchCall?
        ): JSONObject {
            System.out.println("------------------")
            System.out.println("FB RESPONSE:")
            System.out.println("${resp}")
            val code = resp.code()
            val body: ResponseBody
            val jsonBody: JSONObject
            var data_: JSONObject = JSONObject("{}")
            when (code) {
                190 -> ServeurFBProxy.resetAccess_token(resumeAt, sp = sp, u = u, s = s)
                200 -> {
                    body = resp.body()!!
                    //System.out.println(body)
                    jsonBody = JSONObject(body.string())
                    System.out.println("ResponseBody<JSONObject> : ${jsonBody}")
                    data_ = if (jsonBody.has("data")) JSONObject(jsonBody).put(
                        "data",
                        JSONArray(jsonBody.get("data").toString())
                    )
                    else jsonBody
                }
                400 -> body = ResponseBody.create(
                    MediaType.parse("application/json"),
                    "{}"
                )//ServeurFBProxy.FBunauthorizedRequest()
                else -> throw java.lang.Exception(resp.errorBody().toString())
            }
            System.out.println("------------------")
            return data_
        }

        /*
         * Utilities below
         */

        fun appsecret_proof(access_token: String): String {
            return generateHashWithHmac256(
                access_token,
                ServeurFB.app_secret
            )
        }

        fun resetAccess_token(resumeAt: Int? = -1, sp: SearchParams?, u: User?, s: SearchCall?) {
            this.access_token_ = ""
            this.access_token(resumeAt, "{}", sp, u, s)
        }

        fun restart(
            retry_in: Long,
            resumeAt: Int? = 0,
            data: String? = "{}",
            sp: SearchParams?,
            u: User?,
            s: SearchCall?
        ): Nothing =
            throw EmptyAccessTokenException(retry_in, resumeAt, JSONObject(data), sp, u, s)

        class EmptyAccessTokenException(
            val retry_in: Long,
            val resumeAt: Int? = -1,
            val data: JSONObject? = JSONObject("{}"),
            sp: SearchParams?,
            u: User?,
            s: SearchCall?
        ) :
            ServeurFBProxyException("empty access_token", sp, u?.id, s)

        abstract class ServeurFBProxyException(
            val msg: String,
            val sp: SearchParams?,
            val userId: String?,
            val searchCall: SearchCall?
        ) :
            Exception(msg)

        fun FBunauthorizedRequest(): Nothing = throw FBUnauthorizedRequestException()
        class FBUnauthorizedRequestException :
            Exception("unauthorized call to facebook")
    }
}