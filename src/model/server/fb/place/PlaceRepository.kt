package projetift604.server.fb

import projetift604.model.place.Place


interface Repository<in Id, Entity> {
    fun getAll(): List<Entity>
    fun add(entity: Entity): Place
    fun remove(id: Id)
    fun get(id: Id): Place?
}

class PlaceRepository : Repository<String, Place> {

    private val dataSource = mutableMapOf<String, Place>()

    override fun getAll(): List<Place> {
        return dataSource.values.toList()
    }

    override fun add(entity: Place): Place {
        dataSource[entity.id] = entity
        return entity
    }

    override fun remove(id: String) {
        dataSource.remove(id)
    }

    override fun get(id: String): Place? {
        return dataSource.get(id)
    }
}