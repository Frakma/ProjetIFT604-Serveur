package projetift604.model.server.searchEngine

import SLog
import com.google.gson.annotations.Expose
import org.json.JSONObject
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

                            val id = if (data.has("id")) data.getString("id") else ""
                            val title = if (data.has("title")) data.getString("title") else ""
                            val location = if (data.has("latitude") && data.has("longitude")) Location(
                                data.getString("latitude"),
                                "${data.get("longitude")}"
                            ) else Location()
                            val image: JSONObject? = if (data.has("image")) data.getJSONObject("image") else null
                            val performers: JSONObject? =
                                if (data.has("performers")) data.getJSONObject("performers") else null
                            val url: String? = if (data.has("url")) data.getString("url") else ""
                            val startTime: String? = if (data.has("start_time")) data.getString("start_time") else ""

                            val nItem = Event.create(id, title, location, image, performers, url, startTime)
                            SLog.log("$i: [$nItem]")
                            SLog.log(
                                "maps url: https://www.google.fr/maps/place/${nItem.location.latitude},${nItem.location.longitude}",
                                true
                            )
                            //SLog.log(item.keySet().toString(), true)

                            /*
                            SLog.changeIndent(SLog.Companion.INDENT.PLUS)
                            SLog.log("id: ${nItem.id}")
                            SLog.log("title: ${nItem.title}")
                            SLog.log("maps url: https://www.google.fr/maps/place/${nItem.location.latitude},${nItem.location.longitude}")
                            SLog.log("url: ${nItem.url}")
                            SLog.log("image: ${nItem.image?.getString("url")}")
                            SLog.log("start time: ${nItem.startTime}")
                            SLog.changeIndent(SLog.Companion.INDENT.MINUS)
                            */
                            eventRepository.add(nItem)
                        }
                        SLog.changeIndent(SLog.Companion.INDENT.MINUS)
                        val data_ = JSONObject(eventRepository.getAll())
                        //val paging = data.getJSONObject("paging")
                        //TODO("paging")
                        return search(data_, 3, sp, user, searchCall)
                    }
                    return search(data, 3, sp, user, searchCall)
                }
                else -> {
                    SLog.log(data.toString())
                    return data
                }
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
        fun extract(ps: Map<String, List<String>>): SearchParams {
            System.out.println(ps)
            val type: String = if (ps.containsKey("type")) ps.get("type")!!.get(0) else ""
            val startAtPhase: Int = if (ps.containsKey("startAtPhase")) ps.get("startAtPhase")!!.get(0).toInt() else 0
            val stopAtPhase: Int = if (ps.containsKey("stopAtPhase")) ps.get("stopAtPhase")!!.get(0).toInt() else -1
            val callerObjectId: String = if (ps.containsKey("callerObjectId")) ps.get("callerObjectId")!!.get(0) else ""
            val limit: Int = if (ps.containsKey("limit")) ps.get("limit")!!.get(0).toInt() else 10
            val offset: Int = if (ps.containsKey("offset")) ps.get("offset")!!.get(0).toInt() else 0

            val data_ = JSONObject(ps.get("data")!!.get(0))
            val center: Location = if (data_.has("center")) Location.create(data_.getString("center")) else Location()
            val distance: String = if (data_.has("distance")) data_.getInt("distance").toString() else "1000"
            val data: SearchParamsData =
                if (data_.has("data")) SearchParamsData(center, distance) else SearchParamsData()

            val sp = SearchParams(type, startAtPhase, stopAtPhase, callerObjectId, limit, offset, data)
            return sp
        }
    }
}


