package projetift604.database

import Artists
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Users: IntIdTable() {
    val name = varchar("name", 50)
    val artist = reference("artist", Artists)
    val preference = reference("preference", Preferences)
}

class User(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var name by Users.name
    var artist by User referencedOn Users.artist
    var preference by User referencedOn Users.preference
}