package fr.nlaurent.discordbot.sharestocks.commands

import discord4j.core.event.domain.InteractionCreateEvent

abstract class AbstractCommand(val event: InteractionCreateEvent) {

    val guild = event.interaction.guild.block() ?: throw Exception("Failed to retrieve guild")
    val channel = event.interaction.channel.block() ?: throw Exception("Failed to retrieve channel")
    val caller = event.interaction.member.orElseThrow { Exception("Failed to retrieve caller member") }

    protected fun reply(message: String) {
        event.reply(message).subscribe()
    }

    protected fun replyEphemeral(message: String) {
        event.replyEphemeral(message).subscribe()
    }

    abstract fun process()

}