package me.shedaniel.smoothscrollingeverywhere

import io.github.prospector.modmenu.api.ModMenuApi
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.EasingMethod
import me.shedaniel.clothconfig2.impl.EasingMethods
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder
import me.shedaniel.smoothscrollingeverywhere.SmoothScrollingEverywhere.bounceBackMultiplier
import me.shedaniel.smoothscrollingeverywhere.SmoothScrollingEverywhere.easingMethod
import me.shedaniel.smoothscrollingeverywhere.SmoothScrollingEverywhere.scrollDuration
import me.shedaniel.smoothscrollingeverywhere.SmoothScrollingEverywhere.scrollStep
import net.minecraft.client.gui.screen.Screen
import net.minecraft.util.Identifier
import java.util.function.Function

object SmoothScrollingEverywhereMM : ModMenuApi {
    override fun getModId(): String = "smoothscrollingeverywhere"
    override fun getConfigScreenFactory(): Function<Screen, out Screen> = Function { parent ->
        val builder = ConfigBuilder.create().setParentScreen(parent).setTitle("Smooth Scrolling Everywhere Config")
        builder.defaultBackgroundTexture = Identifier("minecraft:textures/block/oak_planks.png")
        val scrolling = builder.getOrCreateCategory("Scrolling")
        val entryBuilder = ConfigEntryBuilder.create()
        scrolling.addEntry(
                entryBuilder.startDropdownMenu("Easing Method", DropdownMenuBuilder.TopCellElementBuilder.of(easingMethod, { str -> EasingMethods.getMethods().firstOrNull { it.toString().equals(str, true) } }), DropdownMenuBuilder.CellCreatorBuilder.of())
                        .setDefaultValue(EasingMethod.EasingMethodImpl.QUART)
                        .setSelections(EasingMethods.getMethods())
                        .setSaveConsumer { easingMethod = it as EasingMethod }
                        .build()
        )
        scrolling.addEntry(
                entryBuilder.startLongSlider("Scroll Duration", scrollDuration, 0, 5000)
                        .setTextGetter { integer -> if (integer <= 0) "Value: Disabled" else if (integer > 1500) String.format("Value: %.1fs", integer / 1000f) else "Value: " + integer + "ms" }
                        .setDefaultValue(1000)
                        .setSaveConsumer { scrollDuration = it }
                        .build()
        )
        scrolling.addEntry(
                entryBuilder.startDoubleField("Scroll Step", scrollStep)
                        .setDefaultValue(16.0)
                        .setSaveConsumer { scrollStep = it }
                        .build()
        )
        scrolling.addEntry(
                entryBuilder.startDoubleField("Bounce Multiplier", bounceBackMultiplier)
                        .setDefaultValue(0.85)
                        .setSaveConsumer { bounceBackMultiplier = it }
                        .build()
        )
        builder.setSavingRunnable(SmoothScrollingEverywhere::saveConfig).build()
    }
}