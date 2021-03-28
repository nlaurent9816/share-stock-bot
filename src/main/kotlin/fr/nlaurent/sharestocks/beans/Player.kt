package fr.nlaurent.sharestocks.beans

import kotlinx.serialization.Serializable

@Serializable
data class Player(val id: Long, var name: String) {
    override fun equals(other: Any?): Boolean {
        return other is Player && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}