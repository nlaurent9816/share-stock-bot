package fr.nlaurent.discordbot.sharestocks.commands

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandOption.Type.SUB_COMMAND
import discord4j.core.`object`.command.ApplicationCommandOption.Type.USER
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import fr.nlaurent.discordbot.sharestocks.beans.Player
import fr.nlaurent.discordbot.sharestocks.beans.Server
import fr.nlaurent.discordbot.sharestocks.beans.from
import org.slf4j.LoggerFactory

class Status(event: ChatInputInteractionEvent) : AbstractCommand(event) {

    companion object {

        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(Status::class.java)

        fun commandRequest(): ApplicationCommandRequest {
            return ApplicationCommandRequest.builder()
                .name("status")
                .description("Affiche l'état des dettes.")
                .addOption(
                    ApplicationCommandOptionData.builder()
                        .name("users")
                        .description("Affiche la balance de tous les utilisateurs")
                        .type(SUB_COMMAND.value).build()
                )
                .addOption(
                    ApplicationCommandOptionData.builder()
                        .name("debts")
                        .description("Affiches toutes les dettes")
                        .type(SUB_COMMAND.value).build()
                )
                .addOption(
                    ApplicationCommandOptionData.builder()
                        .name("all")
                        .description("Affiche tout pour tout le monde")
                        .type(SUB_COMMAND.value).build()
                )
                .addOption(
                    ApplicationCommandOptionData.builder()
                        .name("user")
                        .description("Affiche le statut de l'appelant ou de l'utilisateur mentionné")
                        .type(SUB_COMMAND.value).addOption(
                            ApplicationCommandOptionData.builder()
                                .name("user")
                                .description("L'utilisateur dont on doit afficher le résumé.")
                                .type(USER.value)
                                .required(false).build()
                        ).build()
                )
                .build()
        }
    }

    private val subCommand = event.interaction.commandInteraction.get().options[0]

    private val serverData = Server.from(guild.id)

    override fun process() {
        LOGGER.info("STATUS command requested by {}", caller.username)

        if (serverData.debts.isEmpty()) {
            reply("Rien de stocké (pour l'instant)")
            LOGGER.info("STATUS command ended successfully")
            return
        }

        when (subCommand.name) {
            "user" -> {
                reply(statusUser(resolveUser()))
            }
            "users" -> {
                reply(statusUsers())
            }
            "debts" -> {
                reply(statusDebts())
            }
            "all" -> {
                reply(statusUsers() + "\n" + statusDebts())
            }
        }
        LOGGER.info("STATUS command ended successfully")
    }

    private fun resolveUser(): Player {

        val optionalUser = subCommand.getOption("user")

        return if (optionalUser.isPresent) {
            val user =
                optionalUser.get().value.get().asUser().block() ?: throw Exception("Failed to get the target user")
            val member = user.asMember(guild.id).block() ?: throw Exception("Failed to get the target member")
            serverData.getPlayerOrNew(member)
        } else {
            serverData.getPlayerOrNew(caller)
        }
    }

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

    private fun statusDebts(): String {
        return buildString {
            append("**Statut dettes** :\n")
            serverData.debts.forEach { append("* ").append(it).append("\n") }
        }
    }

    private fun statusUsers(): String {
        return buildString {
            append("**Statut utilisateurs** :\n")
            serverData.players.forEach { (_, player) ->
                val debts = serverData.getPlayerDebts(player)
                val totalDebt = debts.sumOf { it.stocksCount }
                val accounts = serverData.getPlayerAccounts(player)
                val totalAccount = accounts.sumOf { it.stocksCount }
                append("* **${player.name}** : Balance : ${totalAccount - totalDebt}, Dette totale : $totalDebt, Créance totale : $totalAccount\n")
            }
        }
    }

}