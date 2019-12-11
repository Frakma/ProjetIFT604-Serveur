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
                        System.out.println("new access_token")
                        serveurFB.access_token_ = json.getString("access_token")
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
            val access_token = serveurFB.access_token()
            val appsecret_proof = serveurFB.appsecret_proof(access_token)
            return serveurFB.searchForPlaces_(center, distance, q, fields, limit, access_token, appsecret_proof)
        }
    }
}