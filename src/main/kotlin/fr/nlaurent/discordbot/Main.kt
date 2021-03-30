package fr.nlaurent.discordbot.sharestocks

import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import fr.nlaurent.discordbot.Properties
import fr.nlaurent.discordbot.sharestocks.beans.*
import fr.nlaurent.discordbot.sharestocks.commands.Steal
import kotlin.io.path.ExperimentalPathApi


@ExperimentalPathApi
fun main() {

    val client = DiscordClient.create(Properties.botToken)
    val gateway = client.login().block()

    gateway.on(MessageCreateEvent::class.java)
        .filter { it.message.content.startsWith("!voleur") || it.message.content.startsWith("!vol") }
        .subscribe { Steal(it).process() }


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
    gateway.on(MessageCreateEvent::class.java).filter { it.message.content.equals("!ping", true) }
        .subscribe { event: MessageCreateEvent ->
            val channel: MessageChannel? = event.message.channel.block()
            channel?.run { createMessage("Pong!").subscribe() }
        }
    gateway.onDisconnect().block()
}