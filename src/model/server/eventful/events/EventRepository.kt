package projetift604.model.server.eventful.events

interface Repository<in Id, Entity> {
    fun getAll(): List<Entity>
    fun add(entity: Entity): Event
    fun remove(id: Id)
    fun get(id: Id): Event?
}

class EventRepository : Repository<String, Event> {

    private val dataSource = mutableMapOf<String, Event>()

    override fun getAll(): List<Event> {
        return dataSource.values.toList()
    }

    override fun add(entity: Event): Event {
        dataSource[entity.id] = entity
        return entity
    }

    override fun remove(id: String) {
        dataSource.remove(id)
    }

    override fun get(id: String): Event? {
        return dataSource.get(id)
    }
}