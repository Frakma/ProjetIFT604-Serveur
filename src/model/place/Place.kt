package projetift604.model.place

import org.json.JSONObject


data class Place_Page(
    val dunno: String? = ""
)

data class Location(
    val latitude: String? = "0.0",
    val longitude: String? = "0.0",
    val value: JSONObject? = JSONObject("{}"),
    val asOneString: String? = latitude + longitude
)

data class Place_Info(
    val pageId: String? = "",
    val location: Location? = Location(),
    var page: Place_Page? = Place_Page()
)

data class Place(
    val id: String,
    val name: String,
    var place_info: Place_Info? = Place_Info()
)