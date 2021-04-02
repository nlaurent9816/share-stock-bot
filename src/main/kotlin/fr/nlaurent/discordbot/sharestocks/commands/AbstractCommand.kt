package fr.nlaurent.discordbot.sharestocks.commands

import discord4j.core.event.domain.message.MessageCreateEvent

abstract class AbstractCommand(val event: MessageCreateEvent) {

    companion object {
        private val BLANK_CHAR_REGEX = Regex("\\s+")
    }

    val message = event.message
    val guild = event.guild.block() ?: throw Exception("Failed to retrieve guild")
    val channel = message.channel.block() ?: throw Exception("Failed to retrieve channel")
    val caller = message.authorAsMember.block() ?: throw Exception("Failed to retrieve caller member")

    protected fun sendMessage(message: String) {
        channel.createMessage(message).subscribe()
    }

    protected fun splitParameters(): List<String> {
        return message.content.split(BLANK_CHAR_REGEX)
    }

    abstract fun process()

}