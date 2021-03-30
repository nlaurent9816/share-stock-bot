package fr.nlaurent.discordbot.sharestocks.commands

import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.message.MessageCreateEvent
import fr.nlaurent.discordbot.sharestocks.beans.Debt
import fr.nlaurent.discordbot.sharestocks.beans.Server
import fr.nlaurent.discordbot.sharestocks.beans.from
import fr.nlaurent.discordbot.sharestocks.beans.save
import kotlin.io.path.ExperimentalPathApi

class Steal(event: MessageCreateEvent) : AbstractCommand(event) {

    companion object {
        private const val USAGE = "Usage: !voleur @voleur [nombre de stocks volés]"
    }

    var voleur: Member? = null

    var stolenStockCount: Long = 1

    private fun verify(): Boolean {
        val parameters = splitParameters()
        if (parameters.size < 2 || parameters.size > 3) {
            sendMessage(USAGE)
            return false
        }

        voleur = message.userMentions.blockFirst()?.let { guild.getMemberById(it.id).block() }
        if (voleur == null) {
            sendMessage(USAGE)
            return false
        }

        stolenStockCount = getStockCount(parameters)
        if (stolenStockCount > 9) {
            sendMessage("Tu peux te faire voler que 9 stocks maximum par commande.")
            return false
        }

        if (stolenStockCount < 1) {
            sendMessage("Tu dois te faire voler au moins 1 stock.")
            return false
        }

        if (voleur == caller) {
            sendMessage("Tu ne peux pas te voler toi-même !")
            return false
        }
        return true
    }

    private fun getStockCount(parameters: List<String>): Long {
        return if (parameters.size >= 3) {
            parameters[2].toLongOrNull() ?: 1
        } else 1
    }

    @ExperimentalPathApi
    override fun process() {

        if (!verify()) return

        val server = Server.from(guild.id)
        val voleur = server.getPlayerOrNew(this.voleur!!)
        val victime = server.getPlayerOrNew(caller)

        val newDebt = Debt(voleur, victime, stolenStockCount)

        server.debts.firstOrNull {
            it.concern(voleur, victime)
        }?.let {
            it.addDebt(newDebt)
            if (it.stocksCount == 0L) {
                server.debts.remove(it)
            }
        } ?: server.debts.add(newDebt)

        sendMessage("${voleur.name} a volé $stolenStockCount stock${if (stolenStockCount != 1L) "s" else ""} à ${victime.name} !")
        server.save()
    }
}