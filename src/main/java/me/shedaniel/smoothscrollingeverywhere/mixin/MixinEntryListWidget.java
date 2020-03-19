package me.shedaniel.smoothscrollingeverywhere.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.math.Rectangle;
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

import static me.shedaniel.clothconfig2.ClothConfigInitializer.clamp;

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
    protected abstract int getScrollbarPositionX();
    
    @Shadow
    public abstract double getScrollAmount();
    
    @Inject(method = "setScrollAmount", at = @At("HEAD"))
    public void setScrollAmount(double double_1, CallbackInfo callbackInfo) {
        scrollAmount = clamp(double_1, getMaxScroll());
        target = clamp(double_1, getMaxScroll());
    }
    
    @Inject(method = "mouseScrolled", cancellable = true, at = @At("HEAD"))
    public void mouseScrolled(double double_1, double double_2, double double_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
        callbackInfoReturnable.setReturnValue(true);
    }
    
    @Unique
    public void offset(double value, boolean animated) {
        scrollTo(target + value, animated);
    }
    
    @Unique
    public void scrollTo(double value, boolean animated) {
        scrollTo(value, animated, ClothConfigInitializer.getScrollDuration());
    }
    
    @Unique
    public void scrollTo(double value, boolean animated, long duration) {
        target = clamp(value, getMaxScroll());
        
        if (animated) {
            start = System.currentTimeMillis();
            this.duration = duration;
        } else
            scrollAmount = target;
    }
    
    @Inject(method = "render", at = @At("HEAD"))
    public void render(int int_1, int int_2, float delta, CallbackInfo callbackInfo) {
        double[] target = new double[]{this.target};
        this.scrollAmount = ClothConfigInitializer.handleScrollingPosition(target, this.scrollAmount, this.getMaxScroll(), delta, (double) this.start, (double) this.duration);
        this.target = target[0];
    }
    
    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/EntryListWidget;getMaxScroll()I", ordinal = 0, shift = At.Shift.AFTER),
            cancellable = true)
    public void renderScrollbar(int int_1, int int_2, float float_1, CallbackInfo callbackInfo) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int scrollbarPositionMinX = this.getScrollbarPositionX();
        int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            int height = (this.bottom - this.top) * (this.bottom - this.top) / this.getMaxPosition();
            height = MathHelper.clamp(height, 32, this.bottom - this.top - 8);
            height = (int) ((double) height - Math.min(this.scrollAmount < 0.0D ? (int) (-this.scrollAmount) : (this.scrollAmount > (double) this.getMaxScroll() ? (int) this.scrollAmount - this.getMaxScroll() : 0), (double) height * 0.95D));
            height = Math.max(10, height);
            int minY = Math.min(Math.max((int) this.getScrollAmount() * (this.bottom - this.top - height) / maxScroll + this.top, this.top), this.bottom - height);
            int bottomColor = new Rectangle(scrollbarPositionMinX, minY, scrollbarPositionMaxX - scrollbarPositionMinX, height).contains(int_1, int_2) ? 168 : 128;
            int topColor = new Rectangle(scrollbarPositionMinX, minY, scrollbarPositionMaxX - scrollbarPositionMinX, height).contains(int_1, int_2) ? 222 : 172;
            buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            buffer.vertex(scrollbarPositionMinX, this.bottom, 0.0D).texture(0.0F, 1.0F).color(0, 0, 0, 255).next();
            buffer.vertex(scrollbarPositionMaxX, this.bottom, 0.0D).texture(1.0F, 1.0F).color(0, 0, 0, 255).next();
            buffer.vertex(scrollbarPositionMaxX, this.top, 0.0D).texture(1.0F, 0.0F).color(0, 0, 0, 255).next();
            buffer.vertex(scrollbarPositionMinX, this.top, 0.0D).texture(0.0F, 0.0F).color(0, 0, 0, 255).next();
            tessellator.draw();
            buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            buffer.vertex(scrollbarPositionMinX, minY + height, 0.0D).texture(0.0F, 1.0F).color(bottomColor, bottomColor, bottomColor, 255).next();
            buffer.vertex(scrollbarPositionMaxX, minY + height, 0.0D).texture(1.0F, 1.0F).color(bottomColor, bottomColor, bottomColor, 255).next();
            buffer.vertex(scrollbarPositionMaxX, minY, 0.0D).texture(1.0F, 0.0F).color(bottomColor, bottomColor, bottomColor, 255).next();
            buffer.vertex(scrollbarPositionMinX, minY, 0.0D).texture(0.0F, 0.0F).color(bottomColor, bottomColor, bottomColor, 255).next();
            tessellator.draw();
            buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
            buffer.vertex(scrollbarPositionMinX, minY + height - 1, 0.0D).texture(0.0F, 1.0F).color(topColor, topColor, topColor, 255).next();
            buffer.vertex(scrollbarPositionMaxX - 1, minY + height - 1, 0.0D).texture(1.0F, 1.0F).color(topColor, topColor, topColor, 255).next();
            buffer.vertex(scrollbarPositionMaxX - 1, minY, 0.0D).texture(1.0F, 0.0F).color(topColor, topColor, topColor, 255).next();
            buffer.vertex(scrollbarPositionMinX, minY, 0.0D).texture(0.0F, 0.0F).color(topColor, topColor, topColor, 255).next();
            tessellator.draw();
        }
        this.renderDecorations(int_1, int_2);
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
        callbackInfo.cancel();
    }
}
