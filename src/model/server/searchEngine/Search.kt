package projetift604.model.server.searchEngine

import com.google.gson.annotations.Expose
import io.ktor.http.Parameters
import org.json.JSONObject
import projetift604.config.SLog
import projetift604.config.getJSONObject
import projetift604.config.getJSONString
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
    val keywords: String = "night"
) {
    companion object {
        fun create(json: JSONObject?): SearchParamsData {
            if (json != null) {
                val c = Location(getJSONString(json, "center"))
                val d = getJSONString(json, "distance")!!
                val da = getJSONString(json, "date")!!
                val k = getJSONString(json, "keywords")!!
                return SearchParamsData(c, d, da, k)
            }
            return SearchParamsData()
        }
    }
}

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
            SLog.log("params: ${ps_}")
            val ps = ps_
            /**/
            val type = ps.get("type")!!
            SLog.log(type.toString())
            val startAtPhase = ps.get("startAtPhase")?.toInt()!!
            val stopAtPhase = ps.get("stopAtPhase")?.toInt()!!
            val callerObjectId = ps.get("callerObjectId")!!
            val limit = ps.get("limit")?.toInt()!!
            val offset = ps.get("offset")?.toInt()!!

            val data_ = JSONObject(ps.get("data")!!)
            SLog.log(data_.toString())
            val data = SearchParamsData.create(data_)
            SLog.log(data.toString())


            val sp = SearchParams(type, startAtPhase, stopAtPhase, callerObjectId, limit, offset, data)

            /**/
            //val sp = SearchParams()
            return sp
        }

        fun extract2(str: String): SearchParams {
            SLog.log("params: ${str}")
            val ps = JSONObject(str)
            /**/
            val type = getJSONString(ps, "type")!!
            val startAtPhase = getJSONString(ps, "startAtPhase")?.toInt()!!
            val stopAtPhase = getJSONString(ps, "stopAtPhase")?.toInt()!!
            val callerObjectId = getJSONString(ps, "callerObjectId")!!
            val limit = getJSONString(ps, "limit")?.toInt()!!
            val offset = getJSONString(ps, "offset")?.toInt()!!

            val data_ = getJSONObject(ps, "data")
            SLog.log(data_.toString())
            val data = SearchParamsData.create(data_)
            SLog.log(data.toString())

            val sp = SearchParams(type, startAtPhase, stopAtPhase, callerObjectId, limit, offset, data)
            return sp
        }
    }
}


