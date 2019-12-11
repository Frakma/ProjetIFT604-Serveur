package projetift604.server.fb

import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import org.apache.commons.codec.binary.Hex
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
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


    companion object {
        val app_id = "454036578587951"
        val app_secret = "7b2be4392bf16a72e6ab1826b90ae438"


        @Throws(Exception::class)
        fun generateHashWithHmac256(message: String, key: String): String {
            val sha256_HMAC = Mac.getInstance("HmacSHA256")
            val secret_key = SecretKeySpec(key.toByteArray(charset("UTF-8")), "HmacSHA256")
            sha256_HMAC.init(secret_key)
            return Hex.encodeHexString(sha256_HMAC.doFinal(message.toByteArray(charset("UTF-8"))))
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

