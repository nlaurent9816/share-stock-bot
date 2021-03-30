package fr.nlaurent.discordbot.sharestocks.commands

import discord4j.core.event.domain.message.MessageCreateEvent
import fr.nlaurent.discordbot.sharestocks.beans.Player
import fr.nlaurent.discordbot.sharestocks.beans.Server
import fr.nlaurent.discordbot.sharestocks.beans.from
import kotlin.io.path.ExperimentalPathApi

class Status(event: MessageCreateEvent) : AbstractCommand(event) {

    companion object {
        private const val USAGE = "Usage : !status [@mention|all]"
    }

    private val parameters = splitParameters()
    private val targetUser by lazy { message.userMentions.blockFirst()?.let { guild.getMemberById(it.id).block() } }

    @ExperimentalPathApi
    private val serverData = Server.from(guild.id)

    private fun verify(): Boolean {
        if (parameters.size > 2) {
            sendMessage(USAGE)
            return false
        }

        if (parameters.size > 1 && !parameters[1].equals("all", true) && targetUser == null) {
            sendMessage(USAGE)
            return false
        }
        return true
    }

    @ExperimentalPathApi
    override fun process() {

        if (!verify()) return

        if (serverData.debts.isEmpty()) {
            channel.createMessage("Rien de stocké (pour l'instant)").subscribe()
            return
        }

        when (val user = resolveUser()) {
            (null) -> sendMessage(statusAll())
            else -> sendMessage(statusUser(user))
        }

    }

    @ExperimentalPathApi
    private fun resolveUser(): Player? {
        return when {
            parameters.size == 1 -> {
                serverData.getPlayerOrNew(caller)
            }
            parameters[1].equals("all", true) -> {
                null
            }
            else -> {
                targetUser?.let { serverData.getPlayerOrNew(it) }
            }
        }
    }

    @ExperimentalPathApi
    private fun statusUser(user: Player): String {
        val userDebts = serverData.debts.filter { it.debtor == user }
        val userAccount = serverData.debts.filter { it.creditor == user }

        if (userDebts.isEmpty() && userAccount.isEmpty()) {
            return "Rien d'enregistré pour ${user.name}"
        }

        var totalDebt = 0L
        val debtMessage = buildString {
            if (userDebts.isNotEmpty()) {
                append("**Dettes :**\n")
                userDebts.forEach { append("\t* Doit ${it.stocksCount} stocks à ${it.creditor?.name ?: it.creditorId}\n");totalDebt += it.stocksCount }
                append("*Dette totale : $totalDebt*")
            }
        }

        var totalAccount = 0L
        val accountMessage = buildString {
            if (userAccount.isNotEmpty()) {
                append("**Créances :**\n")
                userAccount.forEach { append("\t* ${it.debtor?.name ?: it.debtorId} doit ${it.stocksCount} stocks\n"); totalAccount += it.stocksCount }
                append("*Créance totale : $totalAccount*")
            }
        }

        return "**Comptes de ${user.name} :**\n" +
                "Balance: ${totalAccount - totalDebt}\n" +
                "$debtMessage\n$accountMessage"

    }

    @ExperimentalPathApi
    private fun statusAll(): String {
        return buildString {
            append("**Statut global** :\n")
            serverData.debts.forEach { append("* ").append(it).append("\n") }
        }
    }
}