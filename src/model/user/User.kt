package projetift604.user

import org.json.JSONObject
import projetift604.server.generateHashWithHmac256
import serveur.ServeurREST
import java.text.SimpleDateFormat
import java.util.*

data class Pref(val name: String, val value: String) {
    /*override fun toString(): String {
        return "{name:${name},value:${value}}"
    }*/
}

data class UserPref(val listPref: MutableMap<String, Pref> = mutableMapOf()) {
    /*override fun toString(): String {
        return "{items:${
            listPref.
                values.
                joinToString(
                    prefix = "[{",
                    postfix = "}]",
                    separator = ","                    
                ) { it.toString() }
        }}"
    }*/
}

data class SearchCall(
    val uri: String,
    val param: JSONObject,
    val date: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date())
) {
    /*override fun toString(): String {
        return "{route:$uri,param:$param,date:$date}"
    }*/
}

data class LastResearch(val researchHash: String, val searchCall: SearchCall, val response: ServeurREST.Response) {
    companion object {
        fun create(
            searchCall: SearchCall,
            response: ServeurREST.Response?
        ): LastResearch {
            return LastResearch(
                generateHashWithHmac256(searchCall.date, searchCall.uri + searchCall.param), searchCall,
                response!!
            )
        }
    }
}

data class UserLastResearchs(val listLastResearchs: MutableMap<String, LastResearch> = mutableMapOf()) {
    /*override fun toString(): String {
        return "{items: ${
            listLastResearchs.
                entries.
                joinToString(
                    prefix = "[{",
                    postfix = "}]",
                    separator = ","
                ){("'${it.key}':${it.value.response.data.length}") }
        }"
    }*/
    fun addLastResearch(searchCall: SearchCall, resp: ServeurREST.Response) {
        val lastResearch = LastResearch.create(searchCall, resp)
        listLastResearchs.set(lastResearch.researchHash, lastResearch)
    }
}


open class User(
    val id: String,
    val name: String = "user",
    val userPref: UserPref = UserPref(),
    val userLastResearchs: UserLastResearchs = UserLastResearchs()
) {
    fun getAllPrefs(): Iterable<Map.Entry<String, Pref>> {
        return userPref.listPref.asIterable()
    }

    fun getPref(name: String): Pref? {
        return userPref.listPref.get(name)
    }

    fun addPref(name: String, value: String) {
        return userPref.listPref.set(name, Pref(name, value))
    }

    fun removePref(name: String): Pref? {
        return userPref.listPref.remove(name)
    }

    fun changePref(name: String, newValue: String): Pref? {
        return userPref.listPref.replace(name, Pref(name, newValue))
    }


    fun getLastResearchs(): Iterable<Map.Entry<String, LastResearch>> {
        return userLastResearchs.listLastResearchs.asIterable()
    }

    fun matchLastResearch(call: SearchCall): LastResearch? {
        val converted = LastResearch.create(call, null)
        return userLastResearchs.listLastResearchs.get(converted.researchHash)
    }

    fun addLastResearch(call: SearchCall, resp: ServeurREST.Response) {
        if (userLastResearchs.listLastResearchs.size == nbMaxLastResearchs) {
            removeLastResearch(userLastResearchs.listLastResearchs.asSequence().elementAt(0).key)
        }
        return userLastResearchs.addLastResearch(call, resp)
    }

    fun removeLastResearch(hash: String) {
        userLastResearchs.listLastResearchs.remove(hash)
    }

    fun updateLastResearch(call: SearchCall, resp: ServeurREST.Response): LastResearch? {
        val converted = LastResearch.create(call, resp)
        return userLastResearchs.listLastResearchs.replace(converted.researchHash, converted)
    }

    /*override fun toString(): String {
        return "{id:${id},name:${name},userPref:${userPref},userLastResearch:${userLastResearchs}}"
    }*/

    companion object {
        private val nbMaxLastResearchs: Int = 10
    }
}