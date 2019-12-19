package projetift604.model.server.searchEngine

import com.google.gson.annotations.Expose
import io.ktor.http.Parameters
import org.json.JSONException
import org.json.JSONObject
import projetift604.config.SLog
import projetift604.model.server.eventful.ServerEventfulProxy
import projetift604.model.server.eventful.events.Event
import projetift604.model.server.eventful.events.EventRepository
import projetift604.user.SearchCall
import projetift604.user.User

class SearchEngine {
    companion object {
        private val eventRepository = EventRepository()
        fun search(
            data: JSONObject,
            resumeAt: Int = -1,
            sp: SearchParams,
            user: User,
            searchCall: SearchCall
        ): JSONObject? {
            SLog.changeIndent(SLog.Companion.INDENT.MINUS)
            SLog.log("[${resumeAt}]:")
            SLog.changeIndent(SLog.Companion.INDENT.PLUS)
            if (sp.stopAtPhase != -1) {
                if (resumeAt > sp.stopAtPhase) {
                    return data
                }
            }
            when (resumeAt) {
                0 -> {
                    SLog.log("data: ${data}")
                    SLog.log("user: ${user}")
                    SLog.log("sp: ${sp}")
                    SLog.log("sc: ${searchCall}")
                    return search(data, 1, sp, user, searchCall)
                }
                1 -> {
                    val events = ServerEventfulProxy.searchForEvents(1, sp, user, searchCall)
                    //val events = ServeurFBProxy.searchForPlaces("45.4037978,-71.8900094","3000","bar","",10,0,sp,user,searchCall)!!
                    return search(events, 2, sp, user, searchCall)
                }
                2 -> {
                    if (data.has("events")) {
                        SLog.log("Events:")
                        SLog.changeIndent(SLog.Companion.INDENT.PLUS)
                        val items_ = data.getJSONObject("events")
                        val items = items_.getJSONArray("event")
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)

                            SLog.log("$i:")
                            //SLog.log("$i: [$nItem]")
                            //SLog.log(item.keySet().toString(), true)

                            /**/
                            SLog.changeIndent(SLog.Companion.INDENT.PLUS)

                            //SLog.log("$item", displayFull = true)
                            val id = getJSONString(item, "id")!!
                            val title = getJSONString(item, "title")!!
                            val location = if (item.has("latitude") && item.has("longitude")) Location(
                                item.getString("latitude"),
                                "${item.get("longitude")}"
                            ) else Location()
                            val image = getJSONObject(item, "image")
                            val performers = getJSONObject(item, "performers")
                            val url = getJSONString(item, "url")
                            val startTime = getJSONString(item, "start_time")

                            val nItem = Event.create(id, title, location, image, performers, url, startTime)

                            SLog.log("id: ${nItem.id}")
                            SLog.log("title: ${nItem.title}")
                            SLog.log("maps url: https://www.google.fr/maps/place/${nItem.location.latitude},${nItem.location.longitude}")
                            SLog.log("url: ${nItem.url}")
                            SLog.log("image: ${nItem.image?.getString("url")}")
                            SLog.log("start time: ${nItem.startTime}")
                            SLog.changeIndent(SLog.Companion.INDENT.MINUS)
                            /**/
                            eventRepository.add(nItem)
                        }
                        SLog.changeIndent(SLog.Companion.INDENT.MINUS)
                        val data_ = JSONObject(eventRepository)
                        //val paging = data.getJSONObject("paging")
                        //TODO("paging")
                        //SLog.log(data_.toString())
                        return search(data_, 3, sp, user, searchCall)
                    }
                    return search(data, 3, sp, user, searchCall)
                }
                else -> {
                    SLog.log("EXITING: ${data}")
                    return data
                }
            }
        }

        private fun getJSONObject(item: JSONObject, key: String): JSONObject? {
            try {
                return if (item.has(key) && !item.isNull(key)) {
                    item.getJSONObject(key)
                } else null
            } catch (e: JSONException) {
                System.err.println(e.localizedMessage)
                return null
            }
        }

        private fun getJSONString(item: JSONObject, key: String): String? {
            try {
                return if (item.has(key) && !item.isNull(key)) {
                    item.getString(key)
                } else null
            } catch (e: JSONException) {
                System.err.println(e.localizedMessage)
                return null
            }
        }

    }
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

data class SearchParamsData(
    val center: Location = Location(),
    val distance: String = "1000",
    val date: String = "today",
    val keyworkds: String = "night"
)

/**
 * params pour une requete effectuée vers "/search"
 */
data class SearchParams(
    val type: String = "",
    val startAtPhase: Int = 0,
    val stopAtPhase: Int = -1,
    val callerObjectId: String = "",
    val limit: Int = 10,
    val offset: Int = 0,
    val data: SearchParamsData = SearchParamsData()
) {
    companion object {
        fun extract(ps_: Parameters): SearchParams {
            //TODO(extract request params)
            SLog.log("params: ${ps_}")
            val ps = JSONObject(ps_.entries()).toMap()
            /*
            val type: String = (ps.get("type") as String?)!!
            val startAtPhase: Int = ((ps.get("startAtPhase") as Int?)!!)
            val stopAtPhase: Int = (ps.get("stopAtPhase") as Int)
            val callerObjectId: String = (ps.get("callerObjectId") as String?)!!
            val limit: Int = (ps.get("limit") as Int)
            val offset: Int = (ps.get("offset") as Int)

            val data_ = JSONObject(ps.get("data")!!)
            val center: Location = if (ps.containsKey("center")) Location.create(ps.get("center") as String) else Location()
            val distance: String = ps.get("distance") as String
            val data: SearchParamsData =
                if (data_.has("data")) SearchParamsData(center, distance) else SearchParamsData()

            val sp = SearchParams(type, startAtPhase, stopAtPhase, callerObjectId, limit, offset, data)

             */
            val sp = SearchParams()
            return sp
        }
    }
}


