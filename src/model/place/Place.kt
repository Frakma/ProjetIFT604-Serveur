package projetift604.model.place

import org.json.JSONObject


data class Place_Page(
    val dunno: String
)

data class Location(
    val latitude: String,
    val longitude: String,
    val value: JSONObject
)

data class Place_Info(
    val pageId: String,
    val location: Location,
    var page: Place_Page?
)

data class Place(
    val id: String,
    val name: String,
    val place_info: Place_Info
)