package fr.nlaurent.discordbot.sharestocks.beans

import discord4j.core.`object`.entity.Member
import kotlinx.serialization.Serializable

@Serializable(with = ServerSerializer::class)
class Server constructor(
    val id: Long, val players: MutableMap<Long, Player> = mutableMapOf(),
    val debts: MutableSet<Debt> = mutableSetOf()
) {

    fun getPlayerOrNew(member: Member): Player {
        return players.getOrPut(member.id.asLong()) {
            Player(
                member.id.asLong(),
                member.displayName
            )
        }.apply { name = member.displayName }

    }
}