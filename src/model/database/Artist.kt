import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import projetift604.database.Genre
import projetift604.database.Genres

object Artists: IntIdTable() {
    val name = varchar("name", 50)
    val genre = reference("genre", Genres)
}

class Artist(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Artist>(Artists)

    var name by Artists.name
    var genre by Genre referencedOn Artists.genre
}