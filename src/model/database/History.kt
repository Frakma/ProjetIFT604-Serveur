package projetift604.database

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.joda.time.LocalDateTime

object Histories: IntIdTable() {
    val query = varchar("query", 50)
    val startDate = LocalDateTime("startDate")
    val endDate = LocalDateTime("endDate")
//    val keywordsList
}

class History(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Genre>(Histories)

    var query by Histories.query
//    var startDate by Histories.startDate.
//    var endDate by Histories.endDate.
}
// requete + datedeb + datefin + liste mots clés + user