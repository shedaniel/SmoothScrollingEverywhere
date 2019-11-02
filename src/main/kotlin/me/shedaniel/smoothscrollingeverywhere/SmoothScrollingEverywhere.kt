package me.shedaniel.smoothscrollingeverywhere

import me.shedaniel.clothconfig2.impl.EasingMethod
import me.shedaniel.clothconfig2.impl.EasingMethods
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.util.*

object SmoothScrollingEverywhere : ClientModInitializer {
    var easingMethod: EasingMethod = EasingMethod.EasingMethodImpl.QUART
    var scrollDuration: Long = 1000
    var scrollStep = 16.0
    var bounceBackMultiplier = 0.24

    override fun onInitializeClient() {
        loadConfig()
    }

    private fun loadConfig() {
        val file = File(FabricLoader.getInstance().configDirectory, "smoothscrollingeverywhere/config.properties")
        try {
            file.parentFile.mkdirs()
            easingMethod = EasingMethod.EasingMethodImpl.QUART
            scrollDuration = 1000
            scrollStep = 16.0
            bounceBackMultiplier = .24
            if (!file.exists()) saveConfig()

            val properties = Properties()
            properties.load(FileInputStream(file))
            val easingStr = properties.getProperty("easingMethod", "QUART")
            easingMethod = EasingMethods.getMethods().firstOrNull { it.toString().equals(easingStr, true) }
                    ?: EasingMethod.EasingMethodImpl.QUART
            scrollDuration = properties.getProperty("scrollDuration")?.toLongOrNull() ?: 1000
            scrollStep = properties.getProperty("scrollStep")?.toDoubleOrNull() ?: 16.0
            bounceBackMultiplier = properties.getProperty("bounceBackMultiplierNew")?.toDoubleOrNull() ?: 0.24
        } catch (e: Exception) {
            e.printStackTrace()
            easingMethod = EasingMethod.EasingMethodImpl.QUART
            scrollDuration = 1000
            scrollStep = 16.0
            bounceBackMultiplier = .24
            try {
                if (file.exists())
                    file.delete()
            } catch (ignored: Exception) {
            }

            saveConfig()
        }
    }

    fun saveConfig() {
        val file = File(FabricLoader.getInstance().configDirectory, "smoothscrollingeverywhere/config.properties")
        try {
            val writer = FileWriter(file, false)
            val properties = Properties()
            properties.setProperty("easingMethod", easingMethod.toString())
            properties.setProperty("scrollDuration", scrollDuration.toString())
            properties.setProperty("scrollStep", scrollStep.toString())
            properties.setProperty("bounceBackMultiplier", bounceBackMultiplier.toString())
            properties.store(writer, null)
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            easingMethod = EasingMethod.EasingMethodImpl.QUART
            scrollDuration = 1000
            scrollStep = 16.0
            bounceBackMultiplier = .24
        }

    }
}