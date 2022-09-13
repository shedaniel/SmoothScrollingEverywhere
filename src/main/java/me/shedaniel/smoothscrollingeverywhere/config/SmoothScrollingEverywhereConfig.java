package me.shedaniel.smoothscrollingeverywhere.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.clothconfig2.impl.EasingMethod;
import me.shedaniel.smoothscrollingeverywhere.SmoothScrollingEverywhere;

@Config(name = SmoothScrollingEverywhere.MODID)
public class SmoothScrollingEverywhereConfig implements ConfigData {
    @ConfigEntry.Gui.NoTooltip()
    @ConfigEntry.Gui.EnumHandler()
    public EasingMethod.EasingMethodImpl easingMethod = EasingMethod.EasingMethodImpl.LINEAR;

    @ConfigEntry.Gui.NoTooltip()
    @ConfigEntry.BoundedDiscrete(min = 0, max = 5000)
    public long scrollDuration = 600;

    @ConfigEntry.Gui.NoTooltip()
    public double scrollStep = 19;

    @ConfigEntry.Gui.NoTooltip()
    public double bounceBackMultiplier = .24;
}