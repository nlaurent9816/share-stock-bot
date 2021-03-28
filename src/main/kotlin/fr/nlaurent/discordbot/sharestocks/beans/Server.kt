package fr.nlaurent.discordbot.sharestocks.beans

import kotlinx.serialization.Serializable

@Serializable(with = ServerSerializer::class)
class Server constructor(
    val id: Long, val players: MutableMap<Long, Player> = mutableMapOf(),
    val debts: MutableSet<Debt> = mutableSetOf()
)