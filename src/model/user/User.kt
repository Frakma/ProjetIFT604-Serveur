package projetift604.user

import io.ktor.http.Parameters
import serveur.ServeurREST
import java.text.SimpleDateFormat
import java.util.*

data class Pref(val name: String, val value: String) {
    override fun toString(): String {
        return "{name:${name}, value:${value}}"
    }
}

data class UserPref(val listPref: MutableMap<String, Pref> = mutableMapOf()) {
    override fun toString(): String {
        return "items:{${listPref.values}}"
    }
}

data class SearchCall(val uri: String, val param: Parameters, val date: Date = Date()) {
    override fun toString(): String {
        return "{route:$uri,param:$param,date:${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(date)}}"
    }
}

data class UserLastResearchs(val listLastResearchs: MutableMap<SearchCall, ServeurREST.Response> = mutableMapOf()) {
    override fun toString(): String {
        return "items:${listLastResearchs.values}"
    }
}


open class User(
    val id: String,
    val name: String = "",
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


    fun getLastResearchs(): Iterable<Map.Entry<SearchCall, ServeurREST.Response>> {
        return userLastResearchs.listLastResearchs.asIterable()
    }

    fun matchLastResearch(call: SearchCall): ServeurREST.Response? {
        return userLastResearchs.listLastResearchs.get(call)
    }

    fun addLastResearch(call: SearchCall, resp: ServeurREST.Response) {
        if (userLastResearchs.listLastResearchs.size == nbMaxLastResearchs) {
            removeLastResearch(userLastResearchs.listLastResearchs.asSequence().elementAt(0).key)
        }
        return userLastResearchs.listLastResearchs.set(call, resp)
    }

    fun removeLastResearch(call: SearchCall): ServeurREST.Response? {
        return userLastResearchs.listLastResearchs.remove(call)
    }

    fun updateLastResearch(call: SearchCall, resp: ServeurREST.Response): ServeurREST.Response? {
        return userLastResearchs.listLastResearchs.replace(call, resp)
    }

    override fun toString(): String {
        return "{id:${id},name:${name},userPref:${userPref},userLastResearch:${userLastResearchs}}"
    }

    companion object {
        private val nbMaxLastResearchs: Int = 10
    }
}