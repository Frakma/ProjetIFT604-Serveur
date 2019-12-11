package projetift604.server.fb

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ServeurFBProxy {
    companion object {
        private val serveurFB = ServeurFB.create()

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
                    if (json.has("access_token")) {
                        access_token_ = json.getString("access_token")
                        System.out.println("new access_token: ${access_token_}")
                    }
                }
            })
        }


        fun searchForPlaces(
            center: String,
            distance: String,
            q: String,
            fields: String,
            limit: String
        ): Call<ResponseBody> {
            val access_token = access_token()
            val appsecret_proof = appsecret_proof(access_token)
            return serveurFB.searchForPlaces_(center, distance, q, fields, limit, access_token, appsecret_proof)
        }

        var access_token_: String = ""
        fun appsecret_proof(access_token: String): String {
            return ServeurFB.generateHashWithHmac256(
                access_token,
                ServeurFB.app_secret
            )
        }

        /**
         * Returns access_token_
         * if this one equals "" then a new access_token is asked (async) and "" is returned
         */
        fun access_token(): String {
            System.out.println("token asked [${access_token_}]")
            if (!access_token_.equals("")) {
                return access_token_
            }
            ServeurFBProxy.getAccess_token()
            restart(500)
            return ""
        }

        fun resetAccess_token() {
            this.access_token_ = ""
        }

        fun restart(retry_in: Long, resumeAt: Int = 0): Nothing = throw EmptyAccessTokenException(retry_in, resumeAt)
        class EmptyAccessTokenException(val retry_in: Long, val resumeAt: Int, val data: ResponseBody? = null) :
            Exception("empty access_token")
    }
}