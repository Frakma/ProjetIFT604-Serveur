package projetift604.server.fb

import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


interface ServeurFB {
    //@Headers("Content-Type:application/json; charset=UTF-8")
    @GET("/oauth/access_token")
    fun getAccess_token_(
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String,
        @Query("grant_type") grant_type: String
    ): Call<ResponseBody>

    //@Headers("Content-Type:application/json; charset=UTF-8")
    @GET("/search?type=place")
    fun searchForPlaces_(
        @Query("center") center: String,
        @Query("distance") distance: String,
        @Query("q") q: String,
        @Query("fields") fields: String,
        @Query("limit") limit: String,
        @Query("access_token") access_token: String,
        @Query("appsecret_proof") appsecret_proof: String
    ): Call<ResponseBody>


    //@Headers("Content-Type:application/json; charset=UTF-8")
    @GET("/{place_id}")
    fun searchForPlaceInfo_(
        @Query("fields") fields: String,
        @Path("place_id") place_id: String,
        @Query("access_token") access_token: String,
        @Query("appsecret_proof") appsecret_proof: String
    ): Call<ResponseBody>


    var access_token_: String
    fun appsecret_proof(access_token: String): String {
        return generateHashWithHmac256(
            access_token,
            app_secret
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
        return access_token_
    }

    fun resetAccess_token() {
        this.access_token_ = ""
    }

    companion object {
        val app_id = "454036578587951"
        val app_secret = "7b2be4392bf16a72e6ab1826b90ae438"


        private fun generateHashWithHmac256(message: String, key: String): String {
            val hashingAlgorithm = "HmacSHA256" //or "HmacSHA1", "HmacSHA512"
            val bytes = hmac(
                hashingAlgorithm,
                key.toByteArray(),
                message.toByteArray()
            )
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

