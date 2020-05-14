package me.shedaniel.smoothscrollingeverywhere;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import net.minecraft.text.LiteralText;

public class SmoothScrollingEverywhereMM implements ModMenuApi {
    @Override
    public String getModId() {
        return "smoothscrollingeverywhere";
    }
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ClothConfigInitializer.getConfigBuilder().setTitle(new LiteralText("Smooth Scrolling Settings")).build();
    }
}
