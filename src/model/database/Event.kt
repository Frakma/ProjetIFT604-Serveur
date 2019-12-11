package projetift604.database

import Artist
import Artists
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.joda.time.LocalDateTime

object Events: IntIdTable() {
    val name = varchar("name", 50)
    val artist = reference("artist", Artists)
    val longitude = integer("longitude")
    val latitude = integer("latitude")
    val startDate = LocalDateTime("startDate")
    val endDate = LocalDateTime("endDate")
}

class Event(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Event>(Events)

    var name by Events.name
    var artist by Artist referencedOn Events.artist
    var longitude by Events.longitude
    var latitude by Events.latitude
//    var startDate by
//    var endDate by
}