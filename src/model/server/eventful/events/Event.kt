package projetift604.model.server.eventful.events

import org.json.JSONObject
import projetift604.model.place.Location


data class Event(
    val id: String,
    val title: String,
    val location: Location,
    val image: JSONObject? = null,
    val performers: JSONObject? = null,
    val url: String? = "",
    val startTime: String? = ""
)