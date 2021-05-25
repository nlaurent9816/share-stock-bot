package fr.nlaurent.discordbot.sharestocks.commands

import discord4j.core.`object`.entity.Member
import discord4j.core.event.domain.InteractionCreateEvent
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.util.ApplicationCommandOptionType.INTEGER
import discord4j.rest.util.ApplicationCommandOptionType.USER
import fr.nlaurent.discordbot.sharestocks.beans.Debt
import fr.nlaurent.discordbot.sharestocks.beans.Server
import fr.nlaurent.discordbot.sharestocks.beans.from
import fr.nlaurent.discordbot.sharestocks.beans.save
import org.slf4j.LoggerFactory

class Steal(event: InteractionCreateEvent) : AbstractCommand(event) {

    companion object {

        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(Steal::class.java)

        fun commandRequest(): ApplicationCommandRequest {
            return ApplicationCommandRequest.builder()
                .name("vol")
                .description("Déclarez le vol d'une de vos vies")
                .addOption(
                    ApplicationCommandOptionData.builder()
                        .name("voleur")
                        .description("Le voleur de vies")
                        .type(USER.value)
                        .required(true).build()
                )
                .addOption(
                    ApplicationCommandOptionData.builder()
                        .name("nb_vies")
                        .description("Le nombre de stocks volés")
                        .type(INTEGER.value)
                        .required(false).build()
                )
                .build()
        }
    }

    private var voleur: Member = event.interaction.commandInteraction.getOption("voleur")
        .map { it.value.get().asUser().block() }
        .map { it.asMember(guild.id).block() }
        .orElseThrow { Exception("Can not get the thief.") }

    private var stolenStockCount: Long = 1

    private fun verify(): Boolean {

        stolenStockCount = getStockCount()
        if (stolenStockCount > 9) {
            replyEphemeral("Tu ne peux te faire voler que 9 stocks maximum par commande.")
            return false
        }

        if (stolenStockCount < 1) {
            replyEphemeral("Tu dois te faire voler au moins 1 stock.")
            return false
        }

        if (voleur == caller) {
            replyEphemeral("Tu ne peux pas te voler toi-même !")
            return false
        }

        if (voleur.isBot) {
            replyEphemeral("Tu joues avec des bots, maintenant ?")
            return false
        }
        return true
    }

    private fun getStockCount(): Long {
        return event.interaction.commandInteraction.getOption("nb_vies").map { it.value.get().asLong() }.orElse(1)
    }

    override fun process() {
        LOGGER.info("STEAL command requested by {}", caller.username)
        if (!verify()) {
            LOGGER.info("STEAL command ended: invalid parameters")
            return
        }

        val server = Server.from(guild.id)
        val voleur = server.getPlayerOrNew(this.voleur)
        val victime = server.getPlayerOrNew(caller)

        val newDebt = Debt(voleur, victime, stolenStockCount)
        LOGGER.info("New debt: {}", newDebt)

        server.debts.firstOrNull {
            it.concern(voleur, victime)
        }?.let {
            it.addDebt(newDebt)
            if (it.stocksCount == 0L) {
                server.debts.remove(it)
            }
        } ?: server.debts.add(newDebt)

        reply("${voleur.name} a volé $stolenStockCount stock${if (stolenStockCount != 1L) "s" else ""} à ${victime.name} !")
        server.save()
        LOGGER.info("STEAL command ended successfully")
    }
}
