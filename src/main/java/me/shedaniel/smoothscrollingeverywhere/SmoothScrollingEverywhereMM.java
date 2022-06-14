package me.shedaniel.smoothscrollingeverywhere;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.text.Text;

public class SmoothScrollingEverywhereMM implements ModMenuApi {
 
    public String getModId() {
        return "smoothscrollingeverywhere";
    }
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> SmoothScrollingEverywhere.getConfigBuilder().setTitle(Text.literal("Smooth Scrolling Settings")).build();
    }
}
