package fr.nlaurent.discordbot

import discord4j.core.DiscordClient
import discord4j.core.event.domain.InteractionCreateEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import fr.nlaurent.discordbot.sharestocks.commands.Status
import fr.nlaurent.discordbot.sharestocks.commands.Steal
import fr.nlaurent.discordbot.sharestocks.commands.oldformat.OldStatus
import fr.nlaurent.discordbot.sharestocks.commands.oldformat.OldSteal


fun main() {

    val client = DiscordClient.create(Properties.botToken)
    val id = client.applicationId.block() ?: throw Exception("Failed to get our ID")
    val gateway = client.login().block() ?: throw Exception("Failed to connect to gateway")

    val commandList: List<String>? =
        client.applicationService.getGlobalApplicationCommands(id).map { it.name() }.collectList().block()

    if (commandList?.contains("ping") != true) {
        val pingCommand = ApplicationCommandRequest.builder().name("ping").description("Test si le bot est là.").build()
        client.applicationService.createGlobalApplicationCommand(id, pingCommand).subscribe()
    }

    if (commandList?.contains("vol") != true) {
        client.applicationService.createGlobalApplicationCommand(id, Steal.commandRequest()).subscribe()
    }


    if (commandList?.contains("status") != true) {
        client.applicationService.createGlobalApplicationCommand(id, Status.commandRequest()).subscribe()
    }

    gateway.on(MessageCreateEvent::class.java)
        .filter { it.message.content.startsWith("!voleur") || it.message.content.startsWith("!vol ") }
        .subscribe { OldSteal(it).process() }

    gateway.on(MessageCreateEvent::class.java)
        .filter { it.message.content.startsWith("!status") }
        .subscribe { OldStatus(it).process() }

    gateway.on(InteractionCreateEvent::class.java).filter { it.commandName.equals("ping") }
        .subscribe {
            println("Pong!")
            it.reply("Pong!").subscribe()
        }

    gateway.on(InteractionCreateEvent::class.java).filter { it.commandName.equals("vol") }
        .subscribe { Steal(it).process() }

    gateway.on(InteractionCreateEvent::class.java).filter { it.commandName.equals("status") }
        .subscribe { Status(it).process() }

    gateway.onDisconnect().block()
}