package me.shedaniel.smoothscrollingeverywhere;

import me.shedaniel.clothconfig2.impl.EasingMethod;
import me.shedaniel.smoothscrollingeverywhere.config.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;

@Environment(EnvType.CLIENT)
public class SmoothScrollingEverywhere implements ModInitializer {
    public static final String MODID = "smoothscrollingeverywhere";
    
    public static EasingMethod getEasingMethod() {
        return ConfigManager.getConfig().easingMethod;
    }
    
    public static long getScrollDuration() {
        return ConfigManager.getConfig().scrollDuration;
    }
    
    public static double getScrollStep() {
        return ConfigManager.getConfig().scrollStep;
    }
    
    public static double getBounceBackMultiplier() {
        return ConfigManager.getConfig().bounceBackMultiplier;
    }
    
    @Override
    public void onInitialize() {
        ConfigManager.registerAutoConfig();
    }
}
