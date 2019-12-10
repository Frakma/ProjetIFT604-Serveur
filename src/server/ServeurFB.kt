package projetift604.server

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


interface ServeurFB {
    @Headers("Content-Type:application/json; charset=UTF-8")
    @GET("/search")
    fun search(
        @Field("type") type: String = "place",
        @Field("center") center: String,
        @Field("distance") distance: String,
        @Field("categories") categories: String,
        @Field("q") q: String,
        @Field("fields") fields: String,
        @Field("access_token") access_token: String = ServeurFB.access_token,
        @Header("appsecret_proof") appsecret_proof: String = ServeurFB.appsecret_proof
    )

    companion object {
        private val access_token = "c6eed4f1bfe5cacd87cbc2cd95553c19"
        private val app_secret = "7b2be4392bf16a72e6ab1826b90ae438"
        private val appsecret_proof = generateHashWithHmac256(access_token, app_secret)


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
                .baseUrl("https://graph.facebook.com/")
                .build()

            return retrofit.create(ServeurFB::class.java)
        }
    }
}

