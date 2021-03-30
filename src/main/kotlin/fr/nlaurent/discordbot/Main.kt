package fr.nlaurent.discordbot.sharestocks

import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import fr.nlaurent.discordbot.Properties
import fr.nlaurent.discordbot.sharestocks.commands.Status
import fr.nlaurent.discordbot.sharestocks.commands.Steal
import kotlin.io.path.ExperimentalPathApi


@ExperimentalPathApi
fun main() {

    val client = DiscordClient.create(Properties.botToken)
    val gateway = client.login().block() ?: throw Exception("Failed to connect to gateway")

    gateway.on(MessageCreateEvent::class.java)
        .filter { it.message.content.startsWith("!voleur") || it.message.content.startsWith("!vol") }
        .subscribe { Steal(it).process() }

    gateway.on(MessageCreateEvent::class.java)
        .filter { it.message.content.startsWith("!status") }
        .subscribe { Status(it).process() }

    gateway.on(MessageCreateEvent::class.java).filter { it.message.content.equals("!ping", true) }
        .subscribe { event: MessageCreateEvent ->
            val channel: MessageChannel? = event.message.channel.block()
            channel?.run { createMessage("Pong!").subscribe() }
        }

    gateway.onDisconnect().block()
}