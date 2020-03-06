package me.shedaniel.smoothscrollingeverywhere;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.util.function.Function;

@Mod(modid = "smooth-scrolling-everywhere", clientSideOnly = true)
public class SmoothScrollingEverywhere {
    private static Function<Double, Double> easingMethod = v -> v;
    private static Property scrollDuration;
    private static Property scrollStep;
    private static Property bounceBackMultiplier;
    private static Property unlimitFps;
    private static Configuration configuration;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        try {
            configuration = new Configuration(new File(event.getModConfigurationDirectory(), "smooth-scrolling-everywhere.cfg"));
            configuration.load();
            scrollDuration = configuration.get("general", "Scroll Duration", 600, null, 0, 5000);
            scrollStep = configuration.get("general", "Scroll Step", 19d, null, 0, 100);
            bounceBackMultiplier = configuration.get("general", "Bounce Back Multiplier", 0.24d);
            unlimitFps = configuration.get("general", "Unlimit FPS", true, "Unlimit FPS (30 FPS) on title screen.");
            getScrollDuration();
            getScrollStep();
            getBounceBackMultiplier();
            isUnlimitFps();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (configuration.hasChanged())
                configuration.save();
        }
    }
    
    public static Function<Double, Double> getEasingMethod() {
        return easingMethod;
    }
    
    public static long getScrollDuration() {
        return scrollDuration == null ? 600 : scrollDuration.getInt();
    }
    
    public static float getScrollStep() {
        return scrollStep == null ? 19 : (float) scrollStep.getDouble();
    }
    
    public static float getBounceBackMultiplier() {
        return bounceBackMultiplier == null ? 0.24f : (float) bounceBackMultiplier.getDouble();
    }
    
    public static boolean isUnlimitFps() {
        return unlimitFps != null && unlimitFps.getBoolean();
    }
    
    public static float handleScrollingPosition(float[] target, float scroll, float maxScroll, float delta, double start, double duration) {
        if (getBounceBackMultiplier() >= 0) {
            target[0] = clamp(target[0], maxScroll);
            if (target[0] < 0) {
                target[0] -= target[0] * (1 - getBounceBackMultiplier()) * delta / 3;
            } else if (target[0] > maxScroll) {
                target[0] = (target[0] - maxScroll) * (1 - (1 - getBounceBackMultiplier()) * delta / 3) + maxScroll;
            }
        } else
            target[0] = clamp(target[0], maxScroll, 0);
        if (!Precision.almostEquals(scroll, target[0], Precision.FLOAT_EPSILON))
            return expoEase(scroll, target[0], Math.min((System.currentTimeMillis() - start) / duration * delta * 3, 1));
        else
            return target[0];
    }
    
    public static float expoEase(float start, float end, double amount) {
        return start + (end - start) * getEasingMethod().apply(amount).floatValue();
    }
    
    public static double clamp(double v, double maxScroll) {
        return clamp(v, maxScroll, 300);
    }
    
    public static double clamp(double v, double maxScroll, double clampExtension) {
        return MathHelper.clamp(v, -clampExtension, maxScroll + clampExtension);
    }
    
    public static float clamp(float v, float maxScroll) {
        return clamp(v, maxScroll, 300);
    }
    
    public static float clamp(float v, float maxScroll, float clampExtension) {
        return MathHelper.clamp(v, -clampExtension, maxScroll + clampExtension);
    }
    
    private static class Precision {
        public static final float FLOAT_EPSILON = 1e-3f;
        public static final double DOUBLE_EPSILON = 1e-7;
        
        public static boolean almostEquals(float value1, float value2, float acceptableDifference) {
            return Math.abs(value1 - value2) <= acceptableDifference;
        }
        
        public static boolean almostEquals(double value1, double value2, double acceptableDifference) {
            return Math.abs(value1 - value2) <= acceptableDifference;
        }
    }
}
