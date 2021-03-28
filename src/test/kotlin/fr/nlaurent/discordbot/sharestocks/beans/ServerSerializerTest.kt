package fr.nlaurent.discordbot.sharestocks.beans

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.io.path.*

internal class ServerSerializerTest {

    val server = Server(123).apply {
        val p1 = Player(1, "Nico")
        val p2 = Player(2, "Cubi")
        val p3 = Player(3, "T~")
        val p4 = Player(4, "Freeks")
        players += setOf(p1, p2, p3, p4).map { it.id to it }

        debts += setOf<Debt>(
            Debt(p1, p2, 2),
            Debt(p2, p3, 7),
            Debt(p1, p3, 1),
            Debt(p4, p1, 5)
        )
    }

    @ExperimentalPathApi
    @BeforeEach
    fun cleanup() {
        Path("build/tmp/servers").forEachDirectoryEntry { it.deleteExisting() }
    }

    @Test
    fun testSerialize() {
        val json = Json.encodeToString(server)
        assert(
            json == "{\"id\":123,\"players\":[{\"id\":1,\"name\":\"Nico\"}," +
                    "{\"id\":2,\"name\":\"Cubi\"},{\"id\":3,\"name\":\"T~\"}," +
                    "{\"id\":4,\"name\":\"Freeks\"}]," +
                    "\"debts\":[{\"debtorId\":1,\"creditorId\":2,\"stocksCount\":2}," +
                    "{\"debtorId\":2,\"creditorId\":3,\"stocksCount\":7}," +
                    "{\"debtorId\":1,\"creditorId\":3,\"stocksCount\":1}," +
                    "{\"debtorId\":4,\"creditorId\":1,\"stocksCount\":5}]}"
        )
        val deserialized = Json.decodeFromString<Server>(json)
        assert(deserialized.players.values.containsAll(server.players.values))
        assert(deserialized.debts.isNotEmpty())
        assert(deserialized.debts.all { it.creditor != null })
        assert(deserialized.debts.all { it.debtor != null })
    }

    @Test
    fun testPlayer() {
        println(server.players[1])
    }

    @ExperimentalPathApi
    @Test
    fun testSave() {
        server.save("build/tmp/servers")
        assert(Path("build/tmp/servers/${server.id}.json").isRegularFile())
        val content = Path("build/tmp/servers/${server.id}.json").readText()
        assert(server.players.values.all { content.contains(it.name) })
    }

    @ExperimentalPathApi
    @Test
    fun testFrom_hit() {
        val newServer = Server.from(118, "src/test/resources")
        assert(newServer.id == 118L)

        assert(newServer.players.values.containsAll(server.players.values))
        assert(newServer.debts.isNotEmpty())
        assert(newServer.debts.all { it.creditor != null })
        assert(newServer.debts.all { it.debtor != null })

    }

    @ExperimentalPathApi
    @Test
    fun testFrom_miss() {
        val newServer = Server.from(9999, "build/tmp/servers")
        assert(Path("build/tmp/servers/${newServer.id}.json").isRegularFile())
        assert(newServer.id == 9999L)

    }


}