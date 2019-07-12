package me.shedaniel.smoothscrollingeverywhere.mixin;

import me.shedaniel.smoothscrollingeverywhere.api.RunSixtyTimesEverySec;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScrollingList.class)
public abstract class MixinGuiScrollingList {
    
    @Unique protected double scrollVelocity;
    @Shadow(remap = false) @Final protected int bottom;
    @Shadow(remap = false) @Final protected int top;
    @Shadow(remap = false) @Final protected int left;
    @Shadow(remap = false) @Final protected int listWidth;
    @Shadow(remap = false) private float scrollDistance;
    @Unique protected RunSixtyTimesEverySec scroller = () -> {
        if (this.scrollVelocity == 0.0D && this.scrollDistance >= 0.0D && this.scrollDistance <= this.getMaxScroll()) {
            this.scrollerUnregisterTick();
        } else {
            double change = this.scrollVelocity * 0.3D;
            if (this.scrollVelocity != 0.0D) {
                this.scrollDistance += change;
                this.scrollVelocity -= this.scrollVelocity * (this.scrollDistance >= 0.0D && this.scrollDistance <= this.getMaxScroll() ? 0.2D : 0.4D);
                if (Math.abs(this.scrollVelocity) < 0.1D) {
                    this.scrollVelocity = 0.0D;
                }
            }
            
            if (this.scrollDistance < 0.0f && this.scrollVelocity == 0.0f) {
                this.scrollDistance = Math.min(this.scrollDistance + (0.0f - this.scrollDistance) * 0.2f, 0.0f);
                if (this.scrollDistance > -0.1f && this.scrollDistance < 0.0f) {
                    this.scrollDistance = 0.0f;
                }
            } else if (this.scrollDistance > this.getMaxScroll() && this.scrollVelocity == 0.0D) {
                this.scrollDistance = Math.max(this.scrollDistance - (this.scrollDistance - this.getMaxScroll()) * 0.2f, this.getMaxScroll());
                if (this.scrollDistance > this.getMaxScroll() && this.scrollDistance < this.getMaxScroll() + 0.1D) {
                    this.scrollDistance = this.getMaxScroll();
                }
            }
        }
    };
    @Shadow(remap = false) private float initialMouseClickY;
    
    @Shadow(remap = false)
    protected abstract int getContentHeight();
    
    @Shadow(remap = false)
    protected abstract void drawScreen(int mouseX, int mouseY);
    
    @Shadow(remap = false)
    protected abstract void applyScrollLimits();
    
    @Unique
    private float getMaxScroll() {
        return this.getContentHeight() - (this.bottom - this.top - 4);
    }
    
    @Redirect(method = "drawScreen(IIF)V", at = @At(value = "INVOKE",
                                                    target = "Lnet/minecraftforge/fml/client/GuiScrollingList;applyScrollLimits()V",
                                                    remap = false), remap = false)
    public void applyScrollLimits(GuiScrollingList guiSlot) {
        if (Mouse.isButtonDown(0))
            applyScrollLimits();
    }
    
    @Inject(method = "handleMouseInput",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I",
                     remap = false), cancellable = true, remap = false)
    public void handleMouseScroll(CallbackInfo callbackInfo) {
        callbackInfo.cancel();
        int i2 = Mouse.getEventDWheel();
        if (i2 != 0) {
            if (i2 > 0) {
                i2 = 1;
            } else if (i2 < 0) {
                i2 = -1;
            }
            if (this.scrollDistance <= this.getMaxScroll() && i2 < 0.0D)
                this.scrollVelocity += 16.0D;
            if (this.scrollDistance >= 0.0D && i2 > 0.0D)
                this.scrollVelocity -= 16.0D;
            if (!this.scroller.isRegistered())
                this.scroller.registerTick();
        }
    }
    
    //    @Inject(method = "drawScreen(IIF)V", at = @At("HEAD"), remap = false)
    //    public void render(int mouseX, int mouseY, float float_1, CallbackInfo callbackInfo) {
    //        if (Mouse.isButtonDown(0))
    //            if (this.initialMouseClickY == -1.0F)
    //                applyScrollLimits();
    //    }
    
    @Unique
    private void scrollerUnregisterTick() {
        this.scroller.unregisterTick();
    }
    
    @Inject(method = "drawScreen(IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/GuiScrollingList;getContentHeight()I",
                     ordinal = 2, shift = At.Shift.AFTER, remap = false), cancellable = true, remap = false)
    public void renderScroll(int int_1, int int_2, float float_1, CallbackInfo callbackInfo) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int scrollbarPositionMaxX = this.left + this.listWidth;
        int scrollbarPositionMinX = scrollbarPositionMaxX - 6;
        int maxScroll = (this.getContentHeight() + 4) - (this.bottom - this.top);
        if (maxScroll > 0) {
            GlStateManager.disableTexture2D();
            int height = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
            height = MathHelper.clamp(height, 32, this.bottom - this.top - 8);
            height = (int) ((double) height - Math.min((double) (this.scrollDistance < 0.0D ? (int) (-this.scrollDistance) : (this.scrollDistance > (double) this.getMaxScroll() ? (int) this.scrollDistance - this.getMaxScroll() : 0)), (double) height * 0.75D));
            int minY = Math.min(Math.max(((int) this.scrollDistance) * (this.bottom - this.top - height) / maxScroll + this.top, this.top), this.bottom - height);
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
        this.drawScreen(int_1, int_2);
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        callbackInfo.cancel();
    }
    
}
