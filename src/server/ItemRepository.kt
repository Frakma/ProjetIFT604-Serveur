package projetift604.server

import projetift604.user.User

interface Repository<in Id, Entity> {
    fun getAll(): List<Entity>
    fun add(entity: Entity): User
    fun remove(id: Id)
    fun get(id: Id): User?
}

class UserRepository : Repository<String, User> {

    private val dataSource = mutableMapOf<String, User>()

    override fun getAll(): List<User> {
        return dataSource.values.toList()
    }

    override fun add(entity: User): User {
        dataSource[entity.id] = entity
        return entity
    }

    override fun remove(id: String) {
        dataSource.remove(id)
    }

    override fun get(id: String): User? {
        return dataSource.get(id)
    }
}