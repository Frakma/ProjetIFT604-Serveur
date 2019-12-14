package projetift604.model.server.eventful

import org.json.JSONObject
import projetift604.model.server.searchEngine.SearchParams
import projetift604.server.eventful.ServeurEventful
import projetift604.user.SearchCall
import projetift604.user.User


class ServerEventfulProxy {
    companion object {
        private val serveurEventful = ServeurEventful.create()

        fun searchForEvents(resumeAt: Int?, sp: SearchParams, u: User, s: SearchCall): JSONObject {
            System.out.println("------------")
            /*val spDate: String = sp.data.date
            System.out.println(spDate)
            val date = ValidDate.validate(spDate)
            System.out.println(date)
            val call = serveurEventful.searchForEvents_(
                app_key = ServeurEventful.app_key,
                center = sp.data.center.toString(),
                distance = sp.data.distance,
                date = date,
                limit = sp.limit.toString(),
                units = "km",
                keyworkds = "",
                count_only = "false",
                sort_oder = "relevance"
            )
            System.out.println(call)
            val resp = call.execute()
            System.out.println(resp)
            System.out.println("------------")
            System.out.println("------------")
            return extractBody(resp, resumeAt, sp, u, s)*/
            return JSONObject()
        }
/*

        fun extractBody(
            resp: Response<ResponseBody>,
            resumeAt: Int? = 0,
            sp: SearchParams?,
            u: User?,
            s: SearchCall?
        ): JSONObject {
            System.out.println("------------------")
            System.out.println("Eventful RESPONSE:")
            System.out.println("${resp}")
            val code = resp.code()
            val body: ResponseBody
            val jsonBody: JSONObject
            var data_: JSONObject = JSONObject("{}")
            when (code) {
                //190 -> //ACCESS_TOKEN_ERROR
                200 -> {
                    body = resp.body()!!
                    //System.out.println(body)
                    jsonBody = JSONObject(body.string())
                    System.out.println("ResponseBody<JSONObject> : ${jsonBody}")
                    data_ = if (jsonBody.has("data")) JSONObject(jsonBody).put(
                        "data",
                        JSONArray(jsonBody.get("data").toString())
                    )
                    else jsonBody
                }
                400 -> body = ResponseBody.create(
                    MediaType.parse("application/json"),
                    "{}"
                )//ServeurFBProxy.FBunauthorizedRequest()
                else -> throw java.lang.Exception(resp.errorBody().toString())
            }
            System.out.println("------------------")
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

 */
    }

}