package fr.nlaurent.sharestocks

import discord4j.common.util.Snowflake

data class Player(
    val userId: Snowflake,
    var name: String,
    val casierJudiciaire: CasierJudiciaire = CasierJudiciaire()
) {

    fun volStock(victime: Player) {
        if (victime.casierJudiciaire.hasDebtTo(this)) {
            victime.casierJudiciaire.rendStock(this)
        } else {
            casierJudiciaire.volStock(victime)
        }
    }

    fun volStock(victime: Player, stocksCount: Int) {
        if (victime.casierJudiciaire.hasDebtTo(this, stocksCount.toLong())) {
            val debtToMe = victime.getDebtTo(this)
            if (debtToMe >= stocksCount) {
                victime.casierJudiciaire.rendStock(this, stocksCount.toLong())
            } else {
                victime.casierJudiciaire.rendStock(this, debtToMe)
                this.casierJudiciaire.volStock(victime, stocksCount - debtToMe)
            }
        } else {
            casierJudiciaire.volStock(victime, stocksCount.toLong())
        }

    }

    fun getDebtTo(creancier: Player): Long {
        return casierJudiciaire[creancier] ?: 0
    }

}