package me.shedaniel.smoothscrollingeverywhere.mixin;

import me.shedaniel.smoothscrollingeverywhere.SmoothScrollingEverywhere;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow public GameSettings gameSettings;
    
    @Inject(method = "getLimitFramerate", at = @At("HEAD"), cancellable = true)
    private void getLimitFramerate(CallbackInfoReturnable<Integer> info) {
        if (SmoothScrollingEverywhere.isUnlimitFps())
            info.setReturnValue(this.gameSettings.limitFramerate);
    }
}
