package me.shedaniel.smoothscrollingeverywhere.config;


import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.smoothscrollingeverywhere.SmoothScrollingEverywhere;

public class ConfigManager {
    private static ConfigHolder<SmoothScrollingEverywhereConfig> holder;

    public static void registerAutoConfig() {
        if (holder != null) {
            throw new IllegalStateException("Configuration already registered");
        }

        holder = AutoConfig.register(SmoothScrollingEverywhereConfig.class, JanksonConfigSerializer::new);
        holder.save();
    }

    public static SmoothScrollingEverywhereConfig getConfig() {
        if (holder == null) {
            return new SmoothScrollingEverywhereConfig();
        }

        return holder.getConfig();
    }

    public static void load() {
        if (holder == null) {
            registerAutoConfig();
        }

        holder.load();
    }

    public static void save() {
        if (holder == null) {
            registerAutoConfig();
        }

        holder.save();
    }
}