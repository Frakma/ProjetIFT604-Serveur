package projetift604.model.server.searchEngine

import SLog
import com.google.gson.annotations.Expose
import org.json.JSONObject
import projetift604.user.SearchCall
import projetift604.user.User

fun formatResponse(call: SearchCall, response: Response, user: User?): String {
    user!!.addLastResearch(call, response)
    SLog.changeIndent(SLog.Companion.INDENT.RESET)
    SLog.log("-------------")
    SLog.log("Response:")
    SLog.changeIndent(SLog.Companion.INDENT.PLUS)
    SLog.log("call: ${call}")
    SLog.log("resp: ${response.toString().subSequence(0, 150)}...")
    SLog.log("user: ${user}:")
    SLog.changeIndent(SLog.Companion.INDENT.MINUS)
    SLog.log("-------------")
    return response.data.toString()
}

data class Response(
    @Expose val status: String,
    @Expose val data: JSONObject = JSONObject()
)

