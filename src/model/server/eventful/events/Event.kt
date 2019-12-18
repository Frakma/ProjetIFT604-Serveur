package projetift604.model.server.eventful.events

import com.google.gson.annotations.Expose
import org.json.JSONObject
import projetift604.model.server.searchEngine.Location


data class Event(
    @Expose val id: String,
    @Expose val title: String,
    @Expose val location: Location,
    @Expose val image: JSONObject? = null,
    @Expose val performers: JSONObject? = null,
    @Expose val url: String? = "",
    @Expose val startTime: String? = ""
) {
    companion object {
        fun create(
            id: String,
            title: String,
            location: Location,
            image: JSONObject?,
            performers: JSONObject?,
            url: String?,
            startTime: String?
        ): Event {
            return Event(id, title, location, image, performers, url, startTime)
        }
    }
}