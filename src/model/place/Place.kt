package projetift604.model.place

import com.google.gson.annotations.Expose
import org.json.JSONObject


data class Place_Page(
    @Expose val dunno: JSONObject = JSONObject()
) {
    /*override fun toString(): String {
        return "{dunno:$dunno}"
    }*/
}

data class Location(
    @Expose val latitude: String? = "45.3808166",
    @Expose val longitude: String? = "-71.9265936",
    @Expose val value: JSONObject? = JSONObject("{}")
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
    @Expose val pageId: String? = "",
    @Expose val location: Location? = Location(),
    @Expose var page: Place_Page? = Place_Page()
) {
    /*override fun toString(): String {
        return "{pageId:$pageId,location:$location,page:$page}"
    }*/
}

data class Place(
    @Expose val id: String,
    @Expose val name: String,
    @Expose var place_info: Place_Info? = Place_Info()
) {
    /*override fun toString(): String {
        return "{id:$id,name:$name,place_info:$place_info}"
    }*/
}