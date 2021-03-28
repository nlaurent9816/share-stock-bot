package fr.nlaurent.sharestocks


class CasierJudiciaire {

    private val delitList: MutableMap<Player, Long> = mutableMapOf()

    operator fun get(player: Player) = delitList[player]

    fun volStock(victime: Player, stockCount: Long = 1) {
        delitList[victime] = delitList.getOrDefault(victime, 0) + stockCount
    }

    fun rendStock(victime: Player, stockCount: Long = 1) {
        if (hasDebtTo(victime, stockCount)) {
            delitList[victime] = delitList.getOrDefault(victime, 0) + stockCount
        } else throw IllegalStateException("User has no stocks to give back !")
    }

    fun hasDebtTo(victime: Player, stockCount: Long = 1): Boolean {
        return delitList[victime]?.let { it >= stockCount } ?: false
    }

    fun isEmpty(): Boolean = delitList.isEmpty() || delitList.all { (_, stockCount) -> stockCount == 0L }

    fun printCasier(): String {
        val builder = StringBuilder()
        delitList.forEach { (victime, dette) -> builder.append("\t*$dette stocks à ${victime.name}\n") }
        return builder.toString()
    }
}