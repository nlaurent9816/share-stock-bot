package fr.nlaurent.discordbot.sharestocks.beans

class ServerTest {

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

}