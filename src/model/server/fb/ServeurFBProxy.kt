package projetift604.server.fb

import okhttp3.ResponseBody
import org.json.JSONObject
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
        fun access_token(resumeAt: Int = 0, data: String = "{}", u: User, s: SearchCall): String {
            System.out.println("token asked [${access_token_}]")
            if (!access_token_.equals("")) {
                return access_token_
            }
            ServeurFBProxy.getAccess_token()
            restart(2000, resumeAt, data, u, s)
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
                    response: Response<ResponseBody>?
                ) {
                    val body = response!!.body()!!.string()
                    val json = JSONObject(body)
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
            limit: String,
            u: User,
            s: SearchCall
        ): Response<ResponseBody> {
            val access_token = access_token(1, "{}", u, s)
            val appsecret_proof = appsecret_proof(access_token)
            val call = serveurFB.searchForPlaces_(center, distance, q, fields, limit, access_token, appsecret_proof)
            val resp = call.execute()
            return resp
        }

        fun searchForPlaceInfo(
            placeId: String,
            fields: String,
            d: JSONObject,
            u: User,
            s: SearchCall
        ): Response<ResponseBody> {
            val access_token = access_token(2, d.toString(), u, s)
            val appsecret_proof = appsecret_proof(access_token)
            val call = serveurFB.searchForPlaceInfo_(fields, placeId, access_token, appsecret_proof)
            val resp = call.execute()
            System.out.println(resp)
            return resp
        }

        /*
         * Utilities below
         */

        fun appsecret_proof(access_token: String): String {
            return ServeurFB.generateHashWithHmac256(
                access_token,
                ServeurFB.app_secret
            )
        }

        fun resetAccess_token(resumeAt: Int = 0, u: User, s: SearchCall) {
            this.access_token_ = ""
            this.access_token(resumeAt, "{}", u, s)
        }

        abstract class ServeurFBProxyException(val msg: String, val userId: String, val searchCall: SearchCall) :
            Exception(msg)

        fun restart(retry_in: Long, resumeAt: Int = 0, data: String = "{}", u: User, s: SearchCall): Nothing =
            throw EmptyAccessTokenException(retry_in, resumeAt, JSONObject(data), u, s)

        class EmptyAccessTokenException(
            val retry_in: Long,
            val resumeAt: Int,
            val data: JSONObject = JSONObject("{}"),
            u: User,
            s: SearchCall
        ) :
            ServeurFBProxyException("empty access_token", u.id, s)

        fun FBunauthorizedRequest(): Nothing = throw FBUnauthorizedRequestException()
        class FBUnauthorizedRequestException :
            Exception("unauthorized call to facebook")
    }
}