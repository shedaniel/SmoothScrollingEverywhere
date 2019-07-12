package me.shedaniel.smoothscrollingeverywhere.mixin;

import me.shedaniel.smoothscrollingeverywhere.api.RunSixtyTimesEverySec;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSlot.class)
public abstract class MixinGuiSlot {
    
    @Shadow public int bottom;
    @Shadow public int top;
    @Shadow protected float amountScrolled;
    @Unique protected double scrollVelocity;
    @Unique protected RunSixtyTimesEverySec scroller = () -> {
        if (this.scrollVelocity == 0.0D && this.amountScrolled >= 0.0D && this.amountScrolled <= this.getMaxScroll()) {
            this.scrollerUnregisterTick();
        } else {
            double change = this.scrollVelocity * 0.3D;
            if (this.scrollVelocity != 0.0D) {
                this.amountScrolled += change;
                this.scrollVelocity -= this.scrollVelocity * (this.amountScrolled >= 0.0D && this.amountScrolled <= this.getMaxScroll() ? 0.2D : 0.4D);
                if (Math.abs(this.scrollVelocity) < 0.1D) {
                    this.scrollVelocity = 0.0D;
                }
            }
            
            if (this.amountScrolled < 0.0f && this.scrollVelocity == 0.0f) {
                this.amountScrolled = Math.min(this.amountScrolled + (0.0f - this.amountScrolled) * 0.2f, 0.0f);
                if (this.amountScrolled > -0.1f && this.amountScrolled < 0.0f) {
                    this.amountScrolled = 0.0f;
                }
            } else if (this.amountScrolled > this.getMaxScroll() && this.scrollVelocity == 0.0D) {
                this.amountScrolled = Math.max(this.amountScrolled - (this.amountScrolled - this.getMaxScroll()) * 0.2f, this.getMaxScroll());
                if (this.amountScrolled > this.getMaxScroll() && this.amountScrolled < this.getMaxScroll() + 0.1D) {
                    this.amountScrolled = this.getMaxScroll();
                }
            }
        }
    };
    
    @Shadow
    public abstract int getMaxScroll();
    
    @Shadow
    protected abstract int getScrollBarX();
    
    @Shadow
    public abstract int getAmountScrolled();
    
    @Shadow
    protected abstract void renderDecorations(int mouseXIn, int mouseYIn);
    
    @Shadow
    protected abstract int getContentHeight();
    
    @Shadow
    protected abstract void bindAmountScrolled();
    
    @Shadow
    public abstract boolean getEnabled();
    
    @Unique
    private void scrollerUnregisterTick() {
        this.scroller.unregisterTick();
    }
    
    @Redirect(method = "drawScreen",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSlot;bindAmountScrolled()V"))
    public void bindAmountScrolled(GuiSlot guiSlot) {
        // Now we do nothing here
    }
    
    @Inject(method = "handleMouseInput",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I",
                     remap = false), cancellable = true)
    public void handleMouseScroll(CallbackInfo callbackInfo) {
        if (Mouse.isButtonDown(0) && this.getEnabled())
            bindAmountScrolled();
        else {
            callbackInfo.cancel();
            int i2 = Mouse.getEventDWheel();
            if (i2 != 0) {
                if (i2 > 0) {
                    i2 = 1;
                } else if (i2 < 0) {
                    i2 = -1;
                }
                if (this.amountScrolled <= this.getMaxScroll() && i2 < 0.0D)
                    this.scrollVelocity += 16.0D;
                if (this.amountScrolled >= 0.0D && i2 > 0.0D)
                    this.scrollVelocity -= 16.0D;
                if (!this.scroller.isRegistered())
                    this.scroller.registerTick();
            }
        }
    }
    
    @Inject(method = "drawScreen",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSlot;getMaxScroll()I", ordinal = 0,
                     shift = At.Shift.AFTER), cancellable = true)
    public void render(int int_1, int int_2, float float_1, CallbackInfo callbackInfo) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int scrollbarPositionMinX = this.getScrollBarX();
        int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            int height = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
            height = MathHelper.clamp(height, 32, this.bottom - this.top - 8);
            height = (int) ((double) height - Math.min((double) (this.amountScrolled < 0.0D ? (int) (-this.amountScrolled) : (this.amountScrolled > (double) this.getMaxScroll() ? (int) this.amountScrolled - this.getMaxScroll() : 0)), (double) height * 0.75D));
            int minY = Math.min(Math.max((int) this.getAmountScrolled() * (this.bottom - this.top - height) / maxScroll + this.top, this.top), this.bottom - height);
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos((double) scrollbarPositionMinX, (double) this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            buffer.pos((double) scrollbarPositionMaxX, (double) this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            buffer.pos((double) scrollbarPositionMaxX, (double) this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            buffer.pos((double) scrollbarPositionMinX, (double) this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos((double) scrollbarPositionMinX, (double) (minY + height), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            buffer.pos((double) scrollbarPositionMaxX, (double) (minY + height), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            buffer.pos((double) scrollbarPositionMaxX, (double) minY, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            buffer.pos((double) scrollbarPositionMinX, (double) minY, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            tessellator.draw();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos((double) scrollbarPositionMinX, (double) (minY + height - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            buffer.pos((double) (scrollbarPositionMaxX - 1), (double) (minY + height - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            buffer.pos((double) (scrollbarPositionMaxX - 1), (double) minY, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            buffer.pos((double) scrollbarPositionMinX, (double) minY, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
        }
        this.renderDecorations(int_1, int_2);
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        callbackInfo.cancel();
    }
    
}
