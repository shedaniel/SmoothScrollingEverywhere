package me.shedaniel.smoothscrollingeverywhere.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.clothconfig2.api.RunSixtyTimesEverySec;
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
    
    @Unique protected double scrollVelocity;
    @Shadow protected int bottom;
    @Shadow protected int top;
    @Shadow private double scrollAmount;
    @Unique protected RunSixtyTimesEverySec scroller = () -> {
        if (this.scrollVelocity == 0.0D && this.scrollAmount >= 0.0D && this.scrollAmount <= this.getMaxScroll()) {
            this.scrollerUnregisterTick();
        } else {
            double change = this.scrollVelocity * 0.3D;
            if (this.scrollVelocity != 0.0D) {
                this.scrollAmount += change;
                this.scrollVelocity -= this.scrollVelocity * (this.scrollAmount >= 0.0D && this.scrollAmount <= this.getMaxScroll() ? 0.2D : 0.4D);
                if (Math.abs(this.scrollVelocity) < 0.1D) {
                    this.scrollVelocity = 0.0D;
                }
            }
            
            if (this.scrollAmount < 0.0D && this.scrollVelocity == 0.0D) {
                this.scrollAmount = Math.min(this.scrollAmount + (0.0D - this.scrollAmount) * 0.2D, 0.0D);
                if (this.scrollAmount > -0.1D && this.scrollAmount < 0.0D) {
                    this.scrollAmount = 0.0D;
                }
            } else if (this.scrollAmount > this.getMaxScroll() && this.scrollVelocity == 0.0D) {
                this.scrollAmount = Math.max(this.scrollAmount - (this.scrollAmount - this.getMaxScroll()) * 0.2D, this.getMaxScroll());
                if (this.scrollAmount > this.getMaxScroll() && this.scrollAmount < this.getMaxScroll() + 0.1D) {
                    this.scrollAmount = this.getMaxScroll();
                }
            }
        }
    };
    
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
    private void scrollerUnregisterTick() {
        this.scroller.unregisterTick();
    }
    
    @Inject(method = "setScrollAmount", at = @At("HEAD"))
    public void setScrollAmount(double double_1, CallbackInfo callbackInfo) {
        scrollVelocity = 0;
        if (scroller.isRegistered())
            scrollerUnregisterTick();
    }
    
    @Inject(method = "mouseScrolled", cancellable = true, at = @At("HEAD"))
    public void mouseScrolled(double double_1, double double_2, double double_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (this.scrollAmount <= this.getMaxScroll() && double_3 < 0.0D)
            this.scrollVelocity += 16.0D;
        if (this.scrollAmount >= 0.0D && double_3 > 0.0D)
            this.scrollVelocity -= 16.0D;
        if (!this.scroller.isRegistered())
            this.scroller.registerTick();
        callbackInfoReturnable.setReturnValue(true);
    }
    
    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/EntryListWidget;getMaxScroll()I",
                     ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    public void render(int int_1, int int_2, float float_1, CallbackInfo callbackInfo) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBufferBuilder();
        int scrollbarPositionMinX = this.getScrollbarPosition();
        int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            int height = (this.bottom - this.top) * (this.bottom - this.top) / this.getMaxPosition();
            height = MathHelper.clamp(height, 32, this.bottom - this.top - 8);
            height = (int) ((double) height - Math.min((double) (this.scrollAmount < 0.0D ? (int) (-this.scrollAmount) : (this.scrollAmount > (double) this.getMaxScroll() ? (int) this.scrollAmount - this.getMaxScroll() : 0)), (double) height * 0.75D));
            int minY = Math.min(Math.max((int) this.getScrollAmount() * (this.bottom - this.top - height) / maxScroll + this.top, this.top), this.bottom - height);
            buffer.begin(7, VertexFormats.POSITION_UV_COLOR);
            buffer.vertex((double) scrollbarPositionMinX, (double) this.bottom, 0.0D).texture(0.0D, 1.0D).color(0, 0, 0, 255).next();
            buffer.vertex((double) scrollbarPositionMaxX, (double) this.bottom, 0.0D).texture(1.0D, 1.0D).color(0, 0, 0, 255).next();
            buffer.vertex((double) scrollbarPositionMaxX, (double) this.top, 0.0D).texture(1.0D, 0.0D).color(0, 0, 0, 255).next();
            buffer.vertex((double) scrollbarPositionMinX, (double) this.top, 0.0D).texture(0.0D, 0.0D).color(0, 0, 0, 255).next();
            tessellator.draw();
            buffer.begin(7, VertexFormats.POSITION_UV_COLOR);
            buffer.vertex((double) scrollbarPositionMinX, (double) (minY + height), 0.0D).texture(0.0D, 1.0D).color(128, 128, 128, 255).next();
            buffer.vertex((double) scrollbarPositionMaxX, (double) (minY + height), 0.0D).texture(1.0D, 1.0D).color(128, 128, 128, 255).next();
            buffer.vertex((double) scrollbarPositionMaxX, (double) minY, 0.0D).texture(1.0D, 0.0D).color(128, 128, 128, 255).next();
            buffer.vertex((double) scrollbarPositionMinX, (double) minY, 0.0D).texture(0.0D, 0.0D).color(128, 128, 128, 255).next();
            tessellator.draw();
            buffer.begin(7, VertexFormats.POSITION_UV_COLOR);
            buffer.vertex((double) scrollbarPositionMinX, (double) (minY + height - 1), 0.0D).texture(0.0D, 1.0D).color(192, 192, 192, 255).next();
            buffer.vertex((double) (scrollbarPositionMaxX - 1), (double) (minY + height - 1), 0.0D).texture(1.0D, 1.0D).color(192, 192, 192, 255).next();
            buffer.vertex((double) (scrollbarPositionMaxX - 1), (double) minY, 0.0D).texture(1.0D, 0.0D).color(192, 192, 192, 255).next();
            buffer.vertex((double) scrollbarPositionMinX, (double) minY, 0.0D).texture(0.0D, 0.0D).color(192, 192, 192, 255).next();
            tessellator.draw();
        }
        this.renderDecorations(int_1, int_2);
        GlStateManager.enableTexture();
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlphaTest();
        GlStateManager.disableBlend();
        callbackInfo.cancel();
    }
    
}
