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
    var easingMethod: EasingMethod = EasingMethod.EasingMethodImpl.LINEAR
    var scrollDuration: Long = 600
    var scrollStep = 19.0
    var bounceBackMultiplier = 0.24

    override fun onInitializeClient() {
        loadConfig()
    }

    private fun loadConfig() {
        val file = File(FabricLoader.getInstance().configDirectory, "smoothscrollingeverywhere/config.properties")
        try {
            file.parentFile.mkdirs()
            easingMethod = EasingMethod.EasingMethodImpl.LINEAR
            scrollDuration = 600
            scrollStep = 19.0
            bounceBackMultiplier = .24
            if (!file.exists()) saveConfig()

            val properties = Properties()
            properties.load(FileInputStream(file))
            val easingStr = properties.getProperty("easingMethod1", "LINEAR")
            easingMethod = EasingMethods.getMethods().firstOrNull { it.toString().equals(easingStr, true) }
                    ?: EasingMethod.EasingMethodImpl.LINEAR
            scrollDuration = properties.getProperty("scrollDuration1")?.toLongOrNull() ?: 600
            scrollStep = properties.getProperty("scrollStep1")?.toDoubleOrNull() ?: 19.0
            bounceBackMultiplier = properties.getProperty("bounceBackMultiplier2")?.toDoubleOrNull() ?: 0.24
        } catch (e: Exception) {
            e.printStackTrace()
            easingMethod = EasingMethod.EasingMethodImpl.LINEAR
            scrollDuration = 600
            scrollStep = 19.0
            bounceBackMultiplier = .24
            try {
                if (file.exists())
                    file.delete()
            } catch (ignored: Exception) {
            }
        }
        saveConfig()
    }

    fun saveConfig() {
        val file = File(FabricLoader.getInstance().configDirectory, "smoothscrollingeverywhere/config.properties")
        try {
            val writer = FileWriter(file, false)
            val properties = Properties()
            properties.setProperty("easingMethod1", easingMethod.toString())
            properties.setProperty("scrollDuration1", scrollDuration.toString())
            properties.setProperty("scrollStep1", scrollStep.toString())
            properties.setProperty("bounceBackMultiplier2", bounceBackMultiplier.toString())
            properties.store(writer, null)
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            easingMethod = EasingMethod.EasingMethodImpl.LINEAR
            scrollDuration = 600
            scrollStep = 19.0
            bounceBackMultiplier = .24
        }

    }
}