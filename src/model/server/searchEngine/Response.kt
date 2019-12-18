package projetift604.model.server.searchEngine

import com.google.gson.annotations.Expose
import org.json.JSONObject
import projetift604.user.SearchCall
import projetift604.user.User

fun formatResponse(call: SearchCall, response: Response, user: User?): String {
    user!!.addLastResearch(call, response)
    System.out.println(
        "-------------" + "\n" +
                "call: ${call}" + "\n" +
                "resp: ${response}" + "\n" +
                "user: ${user}" + "\n" +
                "-------------" + "\n"
    )
    return response.toString()
}

data class Response(
    @Expose val status: String,
    @Expose val data: JSONObject = JSONObject()
)

