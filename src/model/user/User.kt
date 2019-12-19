package projetift604.user

import com.google.gson.annotations.Expose
import projetift604.model.server.searchEngine.Response
import projetift604.server.generateHashWithHmac256
import java.text.SimpleDateFormat
import java.util.*

data class Pref(@Expose val name: String, val value: String) {
    /*override fun toString(): String {
        return "{name:${name},value:${value}}"
    }*/
}

data class UserPref(@Expose val listPref: MutableMap<String, Pref> = mutableMapOf()) {
    /*override fun toString(): String {
        return "{items:${
            listPref.values.joinToString(
                    prefix = "[{",
                    postfix = "}]",
                    separator = ","                    
                ) { it.toString() }
        }}"
    }*/
}

data class SearchCall(
    @Expose val uri: String,
    @Expose val param: String,
    @Expose val date: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date())
) {
    /*override fun toString(): String {
        return "{route:$uri,param:$param,date:$date}"
    }*/
}

data class LastResearch(
    @Expose val researchHash: String,
    @Expose val searchCall: SearchCall,
    @Expose val response: Response
) {
    companion object {
        fun create(
            searchCall: SearchCall,
            response: Response?
        ): LastResearch {
            return LastResearch(
                generateHashWithHmac256(searchCall.date, searchCall.uri + searchCall.param), searchCall,
                response!!
            )
        }
    }
}

data class UserLastResearchs(
    @Expose val listLastResearchs: MutableMap<String, LastResearch> = mutableMapOf()
) {
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
    fun addLastResearch(searchCall: SearchCall, resp: Response) {
        val lastResearch = LastResearch.create(searchCall, resp)
        listLastResearchs.set(lastResearch.researchHash, lastResearch)
    }
}


open class User(
    @Expose val id: String,
    @Expose val name: String = "user",
    @Expose val userPref: UserPref = UserPref(),
    @Expose val userLastResearchs: UserLastResearchs = UserLastResearchs()
) {
    fun getAll(): Iterable<Map.Entry<String, Pref>> {
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

    fun addLastResearch(call: SearchCall, resp: Response) {
        if (userLastResearchs.listLastResearchs.size == nbMaxLastResearchs) {
            removeLastResearch(userLastResearchs.listLastResearchs.asSequence().elementAt(0).key)
        }
        return userLastResearchs.addLastResearch(call, resp)
    }

    fun removeLastResearch(hash: String) {
        userLastResearchs.listLastResearchs.remove(hash)
    }

    fun updateLastResearch(call: SearchCall, resp: Response): LastResearch? {
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