package projetift604.server.eventful

import Config
import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.text.SimpleDateFormat

abstract class ValidDate {
    companion object {
        val ALL = "All"
        val FUTURE = "Future"
        val PAST = "Past"
        val TODAY = "Today"
        val LASTWEEK = "Last Week"
        val THISWEEK = "This Week"
        val NEXTWEEK = "Next Week"
        private val EXACT_PATTERN = "YYYYMMDD00-YYYYMMDD00"
        private val EXACT = SimpleDateFormat(EXACT_PATTERN)
        private fun make(s: String): String {
            return EXACT.parse(s).toString()
        }

        fun validate(s: String): String {
            System.out.println("validation date -> [${s}]")
            return when (s) {
                ALL -> ALL
                FUTURE -> FUTURE
                PAST -> PAST
                TODAY -> TODAY
                LASTWEEK -> LASTWEEK
                THISWEEK -> THISWEEK
                NEXTWEEK -> NEXTWEEK
                else -> TODAY
                //"" -> make(s)
            }
        }
    }
}

interface ServeurEventful {
    @Headers("Content-Type:application/json; charset=UTF-8")
    @GET("/events/search")
    fun searchForEvents_(
        @Query("app_key") app_key: String,
        @Query("location") center: String,
        @Query("within") distance: String,
        @Query("date") date: String,
        @Query("keywords") fields: String = "",
        @Query("units") units: String = "km",
        @Query("count_only") count_only: String = "false",
        @Query("sort_order") sort_oder: String = "relevance",
        @Query("page_size") limit: String,
        @Query("change_multi_day_start") startAtDate: String = ""//TODO(a etudier),
    ): Call<ResponseBody>


    companion object {
        val app_key = Config.get().EVENTFUL.APP_KEY


        fun create(): ServeurEventful {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create()
                )
                .addConverterFactory(
                    GsonConverterFactory.create(GsonBuilder().setLenient().create())
                )
                .baseUrl(Config.get().EVENTFUL.DOMAIN)
                .build()

            return retrofit.create(ServeurEventful::class.java)
        }
    }
}

