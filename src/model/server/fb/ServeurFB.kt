package projetift604.server.fb

import Config
import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface ServeurFB {
    @Headers("Content-Type:application/json; charset=UTF-8")
    @GET("/oauth/access_token")
    fun getAccess_token_(
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String,
        @Query("grant_type") grant_type: String
    ): Call<ResponseBody>

    @Headers("Content-Type:application/json; charset=UTF-8")
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


    @Headers("Content-Type:application/json; charset=UTF-8")
    @GET("/{place_id}")
    fun searchForPlaceInfo_(
        @Path("place_id") place_id: String,
        @Query("fields") fields: String,
        @Query("access_token") access_token: String,
        @Query("appsecret_proof") appsecret_proof: String
    ): Call<ResponseBody>


    companion object {
        val app_id = Config.get().FB.APP_ID
        val app_secret = Config.get().FB.APP_SECRET


        fun create(): ServeurFB {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create()
                )
                .addConverterFactory(
                    GsonConverterFactory.create(GsonBuilder().setLenient().create())
                )
                .baseUrl("")
                .build()

            return retrofit.create(ServeurFB::class.java)
        }
    }
}

