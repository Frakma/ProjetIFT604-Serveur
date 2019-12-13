package projetift604.model.place

import org.json.JSONObject


data class Place_Page(
    val dunno: String? = ""
) {
    /*override fun toString(): String {
        return "{dunno:$dunno}"
    }*/
}

data class Location(
    val latitude: String? = "0.0",
    val longitude: String? = "0.0",
    val value: JSONObject? = JSONObject("{}")
) {
    /*override fun toString(): String {
        return "{lat:$latitude,lon:$longitude}"
    }*/
    companion object {
        fun create(str: String): Location {
            val str_spl = str.split(",")
            return Location(str_spl[0], str_spl[1])
        }
    }
}

data class Place_Info(
    val pageId: String? = "",
    val location: Location? = Location(),
    var page: Place_Page? = Place_Page()
) {
    /*override fun toString(): String {
        return "{pageId:$pageId,location:$location,page:$page}"
    }*/
}

data class Place(
    val id: String,
    val name: String,
    var place_info: Place_Info? = Place_Info()
) {
    /*override fun toString(): String {
        return "{id:$id,name:$name,place_info:$place_info}"
    }*/
}