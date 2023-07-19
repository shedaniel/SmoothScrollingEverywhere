package me.shedaniel.smoothscrollingeverywhere.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScrollingContainer;
import me.shedaniel.smoothscrollingeverywhere.EntryListWidgetScroller;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.shedaniel.clothconfig2.api.ScrollingContainer.clampExtension;

@Mixin(EntryListWidget.class)
public abstract class MixinEntryListWidget {
    
    @Unique protected ScrollingContainer scroller = new EntryListWidgetScroller((EntryListWidget) (Object) this);
    @Shadow private double scrollAmount;
    
    @Shadow
    protected abstract void renderDecorations(DrawContext context, int int_1, int int_2);
    
    @Inject(method = "setScrollAmount", at = @At("HEAD"))
    public void setScrollAmount(double double_1, CallbackInfo callbackInfo) {
        scroller.scrollAmount = clampExtension(double_1, scroller.getMaxScroll(), 0);
        scroller.scrollTarget = clampExtension(double_1, scroller.getMaxScroll(), 0);
    }
    
    @Inject(method = "mouseScrolled", cancellable = true, at = @At("HEAD"))
    public void mouseScrolled(double double_1, double double_2, double double_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        scroller.offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
        callbackInfoReturnable.setReturnValue(true);
    }
    
    @Inject(method = "render", at = @At("HEAD"))
    public void render(DrawContext context, int int_1, int int_2, float delta, CallbackInfo callbackInfo) {
        scroller.updatePosition(delta);
        this.scrollAmount = scroller.scrollAmount;
    }
    
    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/EntryListWidget;getMaxScroll()I", ordinal = 0, shift = At.Shift.AFTER),
            cancellable = true)
    public void renderScrollbar(DrawContext context, int int_1, int int_2, float float_1, CallbackInfo callbackInfo) {
        scroller.renderScrollBar(context);
        //RenderSystem.shadeModel(7424);
        //RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
        this.renderDecorations(context, int_1, int_2);
        callbackInfo.cancel();
    }
}
