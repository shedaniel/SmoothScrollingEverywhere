package me.shedaniel.smoothscrollingeverywhere.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.clothconfig2.gui.widget.DynamicEntryListWidget.SmoothScrollingSettings;
import me.shedaniel.clothconfig2.gui.widget.DynamicNewSmoothScrollingEntryListWidget.Precision;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.smoothscrollingeverywhere.SmoothScrollingEverywhere;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntryListWidget.class)
public abstract class MixinEntryListWidget {
    
    @Shadow protected int bottom;
    @Shadow protected int top;
    @Unique protected double target;
    @Unique protected long start;
    @Unique protected long duration;
    @Shadow private double scrollAmount;
    
    @Shadow
    protected abstract int getMaxScroll();
    
    @Shadow
    protected abstract void renderDecorations(int int_1, int int_2);
    
    @Shadow
    protected abstract int getMaxPosition();
    
    @Shadow
    protected abstract int getScrollbarPosition();
    
    @Shadow
    public abstract double getScrollAmount();
    
    @Unique
    public final double clamp(double v) {
        return clamp(v, SmoothScrollingSettings.CLAMP_EXTENSION);
    }
    
    @Unique
    public final double clamp(double v, double clampExtension) {
        return MathHelper.clamp(v, -clampExtension, getMaxScroll() + clampExtension);
    }
    
    @Inject(method = "setScrollAmount", at = @At("HEAD"))
    public void setScrollAmount(double double_1, CallbackInfo callbackInfo) {
        scrollAmount = clamp(double_1);
        target = clamp(double_1);
    }
    
    @Inject(method = "mouseScrolled", cancellable = true, at = @At("HEAD"))
    public void mouseScrolled(double double_1, double double_2, double double_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        offset(SmoothScrollingEverywhere.INSTANCE.getScrollStep() * -double_3, true);
        callbackInfoReturnable.setReturnValue(true);
    }
    
    @Unique
    public void offset(double value, boolean animated) {
        scrollTo(target + value, animated);
    }
    
    @Unique
    public void scrollTo(double value, boolean animated) {
        scrollTo(value, animated, SmoothScrollingEverywhere.INSTANCE.getScrollDuration());
    }
    
    @Unique
    public void scrollTo(double value, boolean animated, long duration) {
        target = clamp(value);
        
        if (animated) {
            start = System.currentTimeMillis();
            this.duration = duration;
        } else
            scrollAmount = target;
    }
    
    @Inject(method = "render", at = @At("HEAD"))
    public void render(int int_1, int int_2, float delta, CallbackInfo callbackInfo) {
        target = clamp(target);
        if (target < 0) {
            target -= target * (1 - SmoothScrollingEverywhere.INSTANCE.getBounceBackMultiplier()) * delta / 3;
        } else if (target > getMaxScroll()) {
            target = (target - getMaxScroll()) * (1 - (1 - SmoothScrollingEverywhere.INSTANCE.getBounceBackMultiplier()) * delta / 3) + getMaxScroll();
        }
        if (!Precision.almostEquals(scrollAmount, target, Precision.FLOAT_EPSILON))
            scrollAmount = (float) expoEase(scrollAmount, target, Math.min((System.currentTimeMillis() - start) / ((double) duration), 1));
        else
            scrollAmount = target;
    }
    
    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/EntryListWidget;getMaxScroll()I",
                     ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    public void renderScrollbar(int int_1, int int_2, float float_1, CallbackInfo callbackInfo) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBufferBuilder();
        int scrollbarPositionMinX = this.getScrollbarPosition();
        int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            int height = (this.bottom - this.top) * (this.bottom - this.top) / this.getMaxPosition();
            height = MathHelper.clamp(height, 32, this.bottom - this.top - 8);
            height = (int) ((double) height - Math.min((double) (this.scrollAmount < 0.0D ? (int) (-this.scrollAmount) : (this.scrollAmount > (double) this.getMaxScroll() ? (int) this.scrollAmount - this.getMaxScroll() : 0)), (double) height * 0.95D));
            height = Math.max(10, height);
            int minY = Math.min(Math.max((int) this.getScrollAmount() * (this.bottom - this.top - height) / maxScroll + this.top, this.top), this.bottom - height);
            int bottomc = new Rectangle(scrollbarPositionMinX, minY, scrollbarPositionMaxX - scrollbarPositionMinX, height).contains(int_1, int_2) ? 168 : 128;
            int topc = new Rectangle(scrollbarPositionMinX, minY, scrollbarPositionMaxX - scrollbarPositionMinX, height).contains(int_1, int_2) ? 222 : 172;
            buffer.begin(7, VertexFormats.POSITION_UV_COLOR);
            buffer.vertex((double) scrollbarPositionMinX, (double) this.bottom, 0.0D).texture(0.0D, 1.0D).color(0, 0, 0, 255).next();
            buffer.vertex((double) scrollbarPositionMaxX, (double) this.bottom, 0.0D).texture(1.0D, 1.0D).color(0, 0, 0, 255).next();
            buffer.vertex((double) scrollbarPositionMaxX, (double) this.top, 0.0D).texture(1.0D, 0.0D).color(0, 0, 0, 255).next();
            buffer.vertex((double) scrollbarPositionMinX, (double) this.top, 0.0D).texture(0.0D, 0.0D).color(0, 0, 0, 255).next();
            tessellator.draw();
            buffer.begin(7, VertexFormats.POSITION_UV_COLOR);
            buffer.vertex((double) scrollbarPositionMinX, (double) (minY + height), 0.0D).texture(0.0D, 1.0D).color(bottomc, bottomc, bottomc, 255).next();
            buffer.vertex((double) scrollbarPositionMaxX, (double) (minY + height), 0.0D).texture(1.0D, 1.0D).color(bottomc, bottomc, bottomc, 255).next();
            buffer.vertex((double) scrollbarPositionMaxX, (double) minY, 0.0D).texture(1.0D, 0.0D).color(bottomc, bottomc, bottomc, 255).next();
            buffer.vertex((double) scrollbarPositionMinX, (double) minY, 0.0D).texture(0.0D, 0.0D).color(bottomc, bottomc, bottomc, 255).next();
            tessellator.draw();
            buffer.begin(7, VertexFormats.POSITION_UV_COLOR);
            buffer.vertex((double) scrollbarPositionMinX, (double) (minY + height - 1), 0.0D).texture(0.0D, 1.0D).color(topc, topc, topc, 255).next();
            buffer.vertex((double) (scrollbarPositionMaxX - 1), (double) (minY + height - 1), 0.0D).texture(1.0D, 1.0D).color(topc, topc, topc, 255).next();
            buffer.vertex((double) (scrollbarPositionMaxX - 1), (double) minY, 0.0D).texture(1.0D, 0.0D).color(topc, topc, topc, 255).next();
            buffer.vertex((double) scrollbarPositionMinX, (double) minY, 0.0D).texture(0.0D, 0.0D).color(topc, topc, topc, 255).next();
            tessellator.draw();
        }
        this.renderDecorations(int_1, int_2);
        GlStateManager.enableTexture();
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlphaTest();
        GlStateManager.disableBlend();
        callbackInfo.cancel();
    }
    
    @Unique
    public double expoEase(double start, double end, double amount) {
        return start + (end - start) * SmoothScrollingEverywhere.INSTANCE.getEasingMethod().apply(amount);
    }
    
}
