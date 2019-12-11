package projetift604.database

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Genres: IntIdTable() {
    val name = varchar("name", 50)
}

class Genre(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Genre>(Genres)

    var name by Genres.name
}