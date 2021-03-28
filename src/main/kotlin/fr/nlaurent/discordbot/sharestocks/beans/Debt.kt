package fr.nlaurent.discordbot.sharestocks.beans

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Debt private constructor() {

    constructor(debtor: Player, creditor: Player, stocksCount: Long = 0L) : this() {
        this.debtor = debtor
        this.creditor = creditor
        this.debtorId = debtor.id
        this.creditorId = creditor.id
        this.stocksCount = stocksCount
    }

    var debtorId: Long = 0L
        private set

    @Transient
    var debtor: Player? = null
        set(value) {
            field = value
            debtorId = value?.id ?: 0L
        }

    var creditorId: Long = 0L
        private set

    @Transient
    var creditor: Player? = null
        set(value) {
            field = value
            creditorId = value?.id ?: 0L
        }

    var stocksCount: Long = 0L
        set(value) {
            if (value < 0) {
                debtor = creditor.also { creditor = debtor }
                field = -value
            } else {
                field = value
            }
        }

    fun concern(player1: Player, player2: Player): Boolean = concern(player1.id, player2.id)

    fun concern(player1: Long, player2: Long): Boolean {
        return creditorId == player1 && debtorId == player2 || creditorId == player2 && debtorId == player1
    }

    operator fun plusAssign(other: Debt) = addDebt(other)

    fun addDebt(other: Debt) {
        require(concern(other.creditorId, other.debtorId)) { "Debt is not on the same players!" }

        if (creditorId == other.debtorId) {
            stocksCount -= other.stocksCount
        } else {
            stocksCount += other.stocksCount
        }

    }

    override fun toString(): String {
        return "${debtor?.name ?: "Inconnu($debtorId)"} doit $stocksCount stocks à ${creditor?.name ?: "Inconnu($creditorId)"}"
    }

}
