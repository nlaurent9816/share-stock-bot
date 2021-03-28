package fr.nlaurent.sharestocks

import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import fr.nlaurent.sharestocks.beans.*
import kotlin.io.path.ExperimentalPathApi


@ExperimentalPathApi
fun main(args: Array<String>) {


    val client = DiscordClient.create("*** BOT key here ***")
    val gateway = client.login().block()

    gateway.on(MessageCreateEvent::class.java).subscribe { event: MessageCreateEvent ->
        val message = event.message
        if (message.content.startsWith("!voleur")) {
            //Vérifications
            val channel: MessageChannel = message.channel.block()
            val parameters = message.content.split(Regex("\\s"))
            if (parameters.size < 2) {
                channel.createMessage("Usage: !voleur @voleur").block()
                return@subscribe
            }
            val voleur = message.userMentions.blockFirst()
            if (voleur == null) {
                channel.createMessage("Usage: !voleur @voleur").block()
                return@subscribe
            }
            val voleurMember = event.guild.block().getMemberById(voleur.id).block()
            val victime = message.authorAsMember.block()
            if (voleur.id == victime.id) {
                channel.createMessage("Tu ne peux pas te voler toi-même !").block()
                return@subscribe
            }

            //Process
            val serverId = event.guildId.orElseThrow { IllegalStateException("No guildId!") }
            val serverData = Server.from(serverId)

            val voleurPlayer = serverData.players.getOrPut(voleurMember.id.asLong()) {
                Player(
                    voleurMember.id.asLong(),
                    voleurMember.displayName
                )
            }.apply { name = voleurMember.displayName }
            val victimePlayer = serverData.players.getOrPut(victime.id.asLong()) {
                Player(
                    victime.id.asLong(),
                    victime.displayName
                )
            }.apply { name = victime.displayName }


            val newDebt = Debt(voleurPlayer, victimePlayer, 1L)
            val filteredDebts = serverData.debts.filter { it.concern(voleurPlayer, victimePlayer) }
            if (filteredDebts.isEmpty()) {
                serverData.debts.add(newDebt)
            } else {
                val existingDebt = filteredDebts.first()
                existingDebt.addDebt(Debt(voleurPlayer, victimePlayer, 1L))
                if (existingDebt.stocksCount == 0L) {
                    serverData.debts.remove(existingDebt)
                }
            }
            channel.createMessage("${voleurPlayer.name} a volé une stock à ${victimePlayer.name} !").block()
            serverData.save()
        }
    }
    gateway.on(MessageCreateEvent::class.java).subscribe { event: MessageCreateEvent ->
        val message = event.message
        if (message.content.startsWith("!status")) {
            val debts = Server.from(event.guildId.orElseThrow { IllegalStateException("No guildId!") }).debts
            val channel = message.channel.block()
            if (debts.isEmpty()) {
                channel.createMessage("Rien de stocké (pour l'instant)").block()
                return@subscribe
            }
            val builder = StringBuilder("Status:\n")
            debts.forEach { builder.append("* ").append(it).append("\n") }
            channel.createMessage(builder.toString()).block()

        }
    }
    gateway.on(MessageCreateEvent::class.java).subscribe { event: MessageCreateEvent ->
        val message = event.message
        if ("!ping" == message.content) {
            val channel: MessageChannel = message.channel.block()
            channel.createMessage("Pong!").block()
        }
    }
    gateway.onDisconnect().block()
}