package projetift604.database

import Artists
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Preferences: IntIdTable() {
    val user = reference("user", Users)
    val artist = reference("artist", Artists)
    val isChosen = bool("isChosen")
}

class Preference(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Preference>(Preferences)

    var user by User referencedOn Preferences.user
    var artist by User referencedOn Preferences.artist
    var isChosen by Preferences.isChosen
}