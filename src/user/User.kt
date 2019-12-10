package projetift604.user

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

open class User(
    val id: String,
    val name: String = "",
    val userPref: UserPref = UserPref()
) {
    fun getPref(name: String): Pref? {
        return userPref.listPref.get(name)
    }

    fun addPref(name: String, value: String) {
        userPref.listPref.set(name, Pref(name, value))
    }

    fun removePref(name: String) {
        userPref.listPref.remove(name)
    }

    fun changePref(name: String, newValue: String) {
        userPref.listPref.replace(name, Pref(name, newValue))
    }

    override fun toString(): String {
        return "{id:${id},name:${name},UserPref:${userPref}}"
    }
}