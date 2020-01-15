package me.shedaniel.smoothscrollingeverywhere;

import io.github.prospector.modmenu.ModMenu;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public class SmoothScrollingEverywhereMM implements ModMenuApi {
    @Override
    public String getModId() {
        return "smoothscrollingeverywhere";
    }
    
    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return parent -> {
            Screen screen = ModMenu.getConfigScreen("cloth-config2", parent);
            if (screen != null) {
                return screen;
            }
            ModMenu.openConfigScreen("cloth-config2");
            screen = MinecraftClient.getInstance().currentScreen;
            MinecraftClient.getInstance().openScreen(parent);
            return screen;
        };
    }
}
