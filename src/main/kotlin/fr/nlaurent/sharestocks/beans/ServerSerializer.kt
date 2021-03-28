package fr.nlaurent.sharestocks.beans

import discord4j.common.util.Snowflake
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.nio.file.StandardOpenOption
import kotlin.io.path.*


object ServerSerializer : KSerializer<Server> {

    @Serializable
    private data class ServerSurrogate(val id: Long, val players: Set<Player>, val debts: Set<Debt>) {
        constructor(server: Server) : this(server.id, server.players.values.toSet(), server.debts)
    }

    override val descriptor = ServerSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Server {
        val surrogate = ServerSurrogate.serializer().deserialize(decoder)
        val playersMap = surrogate.players.map { it.id to it }.toMap()
        val result = Server(surrogate.id, playersMap.toMutableMap(), surrogate.debts.toMutableSet())

        result.debts.forEach {
            it.creditor = playersMap[it.creditorId]
            it.debtor = playersMap[it.debtorId]
        }
        return result
    }

    override fun serialize(encoder: Encoder, value: Server) =
        ServerSurrogate.serializer().serialize(encoder, ServerSurrogate(value))

}

/**
 * Extension functions for Serialization
 */
private const val FILES_LOCATION = "servers"
private val JSON = Json { prettyPrint = true;ignoreUnknownKeys = true }

@ExperimentalPathApi
fun Server.Companion.from(id: Long, location: String = FILES_LOCATION): Server {
    val serverFilePath = Path(location, "$id.json")
    return if (serverFilePath.isRegularFile()) {
        JSON.decodeFromString<Server>(serverFilePath.readText(Charsets.UTF_8))
    } else {
        Server(id)
    }
}

@ExperimentalPathApi
fun Server.Companion.from(id: Snowflake, location: String = FILES_LOCATION): Server = from(id.asLong(), location)

@ExperimentalPathApi
fun Server.save(location: String = FILES_LOCATION) {
    val serverFilePath = Path(location, "$id.json")
    serverFilePath.parent.createDirectories()
    serverFilePath.writeText(
        JSON.encodeToString(this),
        Charsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
    )
}

