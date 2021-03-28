package fr.nlaurent.discordbot

import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.InputStreamReader

object Properties {

    private const val FILENAME = "discordbot.properties"
    private val LOGGER = LoggerFactory.getLogger(Properties.javaClass)
    private val properties: java.util.Properties?

    init {
        var loadedProperty: java.util.Properties? = null
        try {
            val fileResource = javaClass.classLoader.getResourceAsStream(FILENAME) ?: FileInputStream(FILENAME)
            loadedProperty = InputStreamReader(fileResource, Charsets.UTF_8).use {
                java.util.Properties().apply { load(it) }
            }
            LOGGER.info("Properties loaded !")
        } catch (e: Exception) {
            LOGGER.warn("Can not load properties file:", e)
        }
        properties = loadedProperty
    }

    val botToken: String
        get() {
            return properties?.getProperty("bot.token") ?: ""
        }

}
