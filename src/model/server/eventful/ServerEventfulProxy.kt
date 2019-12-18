package projetift604.model.server.eventful

import SLog
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import projetift604.model.server.searchEngine.SearchParams
import projetift604.model.server.searchEngine.ValidDate
import projetift604.server.eventful.ServeurEventful
import projetift604.user.SearchCall
import projetift604.user.User
import retrofit2.Response


class ServerEventfulProxy {
    companion object {
        private val serveurEventful = ServeurEventful.create()

        fun searchForEvents(resumeAt: Int?, sp: SearchParams, u: User, s: SearchCall): JSONObject {
            SLog.log("fetching eventful...")
            val spDate: String = sp.data.date
            val date = ValidDate.validate(spDate)
            val call = serveurEventful.searchForEvents_(
                app_key = ServeurEventful.app_key,
                center = sp.data.center.latitude + ',' + sp.data.center.longitude,
                within = sp.data.distance,
                date = date,
                page_size = sp.limit.toString(),
                page_number = "1",
                units = "km",
                keywords = sp.data.keyworkds,
                sort_oder = "relevance"
            )
            val resp = call.execute()
            //System.out.println(resp)
            //System.out.println("------------")
            return extractBody(resp, resumeAt, sp, u, s)
        }


        fun extractBody(
            resp: Response<ResponseBody>,
            resumeAt: Int? = 0,
            sp: SearchParams?,
            u: User?,
            s: SearchCall?
        ): JSONObject {
            val code = resp.code()
            val body: ResponseBody
            val jsonBody: JSONObject
            var data_: JSONObject = JSONObject("{}")
            when (code) {
                //190 -> //ACCESS_TOKEN_ERROR
                200 -> {
                    body = resp.body()!!
                    jsonBody = JSONObject(body.string())
                    //System.out.println("ResponseBody<JSONObject> : ${jsonBody}")
                    data_ = if (jsonBody.has("data")) JSONObject(jsonBody).put(
                        "data",
                        JSONArray(jsonBody.get("data").toString())
                    )
                    else jsonBody
                }
                else -> body = ResponseBody.create(
                    MediaType.parse("application/json"),
                    "{}"
                )//ServeurFBProxy.FBunauthorizedRequest()
                //else -> throw java.lang.Exception(resp.errorBody().toString())
            }
            SLog.log("Extracted: ${data_.toString().subSequence(0, 150)}")
            return data_
        }


        fun restart(
            retry_in: Long,
            resumeAt: Int? = 0,
            sp: SearchParams?,
            u: User?,
            s: SearchCall?
        ): Nothing =
            throw RestartException(retry_in, resumeAt, sp, u, s)

        class RestartException(
            val retry_in: Long,
            val resumeAt: Int? = -1,
            sp: SearchParams?,
            u: User?,
            s: SearchCall?
        ) :
            ServeurEventfulProxyException("empty access_token", sp, u?.id, s)

        abstract class ServeurEventfulProxyException(
            msg: String,
            val sp: SearchParams?,
            val userId: String?,
            val searchCall: SearchCall?
        ) : Exception(msg)

    }

}