package projetift604.model.server.searchEngine

import SLog
import org.json.JSONObject
import projetift604.model.place.Location
import projetift604.model.place.Place
import projetift604.model.place.Place_Info
import projetift604.model.place.Place_Page
import projetift604.model.server.eventful.ServerEventfulProxy
import projetift604.server.fb.PlaceRepository
import projetift604.server.fb.ServeurFBProxy
import projetift604.user.SearchCall
import projetift604.user.User
import java.text.SimpleDateFormat

val placeRepository = PlaceRepository()
fun search(data: JSONObject, resumeAt: Int = -1, sp: SearchParams, user: User, searchCall: SearchCall): JSONObject? {
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
            /*
            val events = ServeurFBProxy.searchForPlaces(
                "45.4037978,-71.8900094",
                "3000",
                "bar",
                "",
                10,
                0,
                sp,
                user,
                searchCall
            )!!
            */
            return search(events, -1, sp, user, searchCall)
        }
        2 -> {
            System.out.println(data)
            if (data.has("data")) {
                val items = data.getJSONArray("data")
                for (i in 0 until items.length()) {
                    val place = items.getJSONObject(i)
                    val name = place.getString("name")
                    val id = place.getString("id")
                    val structuredPlace = Place(id, name)
                    val fieldsWanted =
                        "name,location,category_list,description,hours,is_verified,payment_options,price_range,rating_count"

                    val place_info = ServeurFBProxy.searchForPlaceInfo(
                        placeId = id,
                        fields = fieldsWanted,
                        d = data,
                        sp = sp,
                        u = user,
                        s = searchCall
                    )!!
                    place.put("place_info", place_info)
                    var location: JSONObject = JSONObject()
                    if (place_info.has("location")) {
                        location = JSONObject(place_info.get("location"))
                    }

                    var page: JSONObject = JSONObject()
                    var pageId: String = ""
                    if (place_info.has("page")) {
                        page = JSONObject(place_info.get("page"))
                        if (page.has("id")) {
                            pageId = page.getString("id")
                        }
                    }

                    structuredPlace.place_info = Place_Info(
                        pageId = pageId,
                        location = Location(
                            latitude = if (location.has("latitude")) location.getString("latitude") else "",
                            longitude = if (location.has("longitude")) location.getString("longitude") else "",
                            value = location
                        ),
                        page = Place_Page(
                            dunno = place!!
                        )
                    )
                    placeRepository.add(structuredPlace)
                }
                val data_ = JSONObject(placeRepository)
                //val paging = data.getJSONObject("paging")
                //TODO("paging")
                return search(data_, 3, sp, user, searchCall)
            }
            return search(data, 3, sp, user, searchCall)
        }
        else -> {
            return data
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

abstract class ValidDate {
    companion object {
        val ALL = "All"
        val FUTURE = "Future"
        val PAST = "Past"
        val TODAY = "Today"
        val LASTWEEK = "Last+Week"
        val THISWEEK = "This+Week"
        val NEXTWEEK = "Next+Week"
        private val EXACT_PATTERN = "YYYYMMDD00-YYYYMMDD00"
        private val EXACT = SimpleDateFormat(EXACT_PATTERN)
        private fun make(s: String): String {
            return EXACT.parse(s).toString()
        }

        fun validate(s: String): String {
            val d = when (s) {
                ALL -> ALL
                FUTURE -> FUTURE
                PAST -> PAST
                TODAY -> TODAY
                LASTWEEK -> LASTWEEK
                THISWEEK -> THISWEEK
                NEXTWEEK -> NEXTWEEK
                else -> FUTURE
                //"" -> make(s)
            }
            SLog.log("validated date: [${s}] -> [${d}]", true)
            return d
        }
    }
}



