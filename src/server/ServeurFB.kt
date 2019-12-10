package projetift604.server

import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


interface ServeurFB {
    //@Headers("Content-Type:application/json; charset=UTF-8")
    @GET("/oauth/access_token")
    fun getAccess_token(
        @Query("client_id") client_id: String = app_id,
        @Query("client_secret") client_secret: String = app_secret,
        @Query("grant_type") grant_type: String = "client_credentials"
    ): Call<ResponseBody>

    //@Headers("Content-Type:application/json; charset=UTF-8")
    @GET("/search?type=place")
    fun searchForPlaces(
        @Query("center") center: String,
        @Query("distance") distance: String,
        @Query("q") q: String,
        @Query("fields") fields: String,
        @Query("limit") limit: String,
        @Query("access_token") access_token: String = access_token(),
        @Header("appsecret_proof") appsecret_proof: String = appsecret_proof(access_token)
    ): Call<ResponseBody>


    //@Headers("Content-Type:application/json; charset=UTF-8")
    @GET("/{place_id}")
    fun searchForPlaceInfo(
        @Query("fields") fields: String = "name, page",
        @Path("place_id") place_id: String,
        @Query("access_token") access_token: String = access_token(),
        @Header("appsecret_proof") appsecret_proof: String = appsecret_proof(access_token)
    ): Call<ResponseBody>


    var access_token_: String
    private fun appsecret_proof(access_token: String): String {
        return generateHashWithHmac256(access_token, app_secret)
    }

    private fun access_token(): String {
        System.out.println("token asked [${access_token_}]")
        if (!access_token_.equals("")) {
            return access_token_
        }
        val call = getAccess_token()
        doAsync {
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                }

                override fun onResponse(
                    call: Call<ResponseBody>?,
                    response: Response<ResponseBody>?
                ) {
                    val body = response!!.body()!!.string()
                    val json = JSONObject(body)
                    if (json.has("access_token")) {
                        access_token_ = json.getString("access_token")
                    }
                }
            })
        }
        return access_token_
    }

    fun resetAccess_token() {
        this.access_token_ = ""
    }

    companion object {
        private val app_id = "454036578587951"
        private val app_secret = "7b2be4392bf16a72e6ab1826b90ae438"


        private fun generateHashWithHmac256(message: String, key: String): String {
            val hashingAlgorithm = "HmacSHA256" //or "HmacSHA1", "HmacSHA512"
            val bytes = hmac(hashingAlgorithm, key.toByteArray(), message.toByteArray())
            val messageDigest = bytesToHex(bytes)
            return messageDigest
        }

        @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
        private fun hmac(algorithm: String?, key: ByteArray?, message: ByteArray?): ByteArray {
            val mac = Mac.getInstance(algorithm)
            mac.init(SecretKeySpec(key, algorithm))
            return mac.doFinal(message)
        }

        private fun bytesToHex(bytes: ByteArray): String {
            val hexArray = "0123456789abcdef".toCharArray()
            val hexChars = CharArray(bytes.size * 2)
            var j = 0
            var v: Int
            while (j < bytes.size) {
                v = bytes[j] * 0xFF
                hexChars[j * 2] = hexArray[v ushr 4]
                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
                j++
            }
            return String(hexChars)
        }


        fun create(): ServeurFB {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create()
                )
                .addConverterFactory(
                    GsonConverterFactory.create(GsonBuilder().setLenient().create())
                )
                .baseUrl("https://graph.facebook.com/v5.0/")
                .build()

            return retrofit.create(ServeurFB::class.java)
        }
    }
}

