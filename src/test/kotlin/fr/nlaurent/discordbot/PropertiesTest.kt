package fr.nlaurent.discordbot

import org.junit.jupiter.api.Test

internal class PropertiesTest {

    @Test
    fun getBotToken() {
        assert(Properties.botToken == "bot token here")
    }
}