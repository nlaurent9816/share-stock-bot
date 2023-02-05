package fr.nlaurent.discordbot.sharestocks.commands

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent

abstract class AbstractCommand(val event: ChatInputInteractionEvent) {

    val guild = event.interaction.guild.block() ?: throw Exception("Failed to retrieve guild")
    val caller = event.interaction.member.orElseThrow { Exception("Failed to retrieve caller member") }

    protected fun reply(message: String) {
        event.reply(message).subscribe()
    }

    protected fun replyEphemeral(message: String) {
        event.reply(message).withEphemeral(true).subscribe()
    }

    abstract fun process()

}