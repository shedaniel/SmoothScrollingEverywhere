package me.shedaniel.smoothscrollingeverywhere.mixin;

import me.shedaniel.smoothscrollingeverywhere.SmoothScrollingEverywhere;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.shedaniel.smoothscrollingeverywhere.SmoothScrollingEverywhere.clamp;

@Mixin(GuiSlot.class)
public abstract class MixinGuiSlot {
    
    @Shadow public int bottom;
    @Shadow public int top;
    @Shadow protected float amountScrolled;
    @Unique protected float target;
    @Unique protected long start;
    @Unique protected long duration;
    
    @Shadow
    public abstract int func_148135_f();
    
    @Shadow
    public abstract boolean getEnabled();
    
    @Shadow
    protected abstract int getScrollBarX();
    
    @Shadow
    protected abstract int getContentHeight();
    
    @Shadow
    public abstract int getAmountScrolled();
    
    @Shadow
    protected abstract void func_148142_b(int p_148142_1_, int p_148142_2_);
    
    @Shadow @Final protected Minecraft mc;
    
    @Redirect(method = "drawScreen",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSlot;bindAmountScrolled()V"))
    public void bindAmountScrolled(GuiSlot guiSlot) {
        amountScrolled = clamp(amountScrolled, func_148135_f());
        target = clamp(target, func_148135_f());
    }
    
    @Inject(method = "handleMouseInput",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I",
                     remap = false), cancellable = true)
    public void handleMouseScroll(CallbackInfo callbackInfo) {
        if (Mouse.isButtonDown(0) && this.getEnabled()) {
            target = amountScrolled = clamp(amountScrolled, func_148135_f(), 0);
        } else {
            int wheel = Mouse.getEventDWheel();
            if (wheel != 0) {
                if (wheel > 0) {
                    wheel = -1;
                } else if (wheel < 0) {
                    wheel = 1;
                }
                
                offset(SmoothScrollingEverywhere.getScrollStep() * wheel, true);
            }
            callbackInfo.cancel();
        }
    }
    
    @Unique
    public void offset(float value, boolean animated) {
        scrollTo(target + value, animated);
    }
    
    @Unique
    public void scrollTo(float value, boolean animated) {
        scrollTo(value, animated, SmoothScrollingEverywhere.getScrollDuration());
    }
    
    @Unique
    public void scrollTo(float value, boolean animated, long duration) {
        target = clamp(value, func_148135_f());
        
        if (animated) {
            start = System.currentTimeMillis();
            this.duration = duration;
        } else
            amountScrolled = target;
    }
    
    @Inject(method = "drawScreen", at = @At("HEAD"))
    public void render(int int_1, int int_2, float delta, CallbackInfo callbackInfo) {
        float[] target = new float[]{this.target};
        this.amountScrolled = SmoothScrollingEverywhere.handleScrollingPosition(target, this.amountScrolled, this.func_148135_f(), 20f / Minecraft.getDebugFPS(), (double) this.start, (double) this.duration);
        this.target = target[0];
    }
    
    @Inject(method = "drawScreen",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiSlot;func_148135_f()I", ordinal = 0,
                     shift = At.Shift.AFTER), cancellable = true)
    public void renderScrollbar(int int_1, int int_2, float float_1, CallbackInfo callbackInfo) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer buffer = tessellator.getWorldRenderer();
        int scrollbarPositionMinX = this.getScrollBarX();
        int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
        int maxScroll = this.func_148135_f();
        if (maxScroll > 0) {
            int height = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
            height = MathHelper.clamp_int(height, 32, this.bottom - this.top - 8);
            height = (int) ((double) height - Math.min(this.amountScrolled < 0.0D ? (int) (-this.amountScrolled) : (this.amountScrolled > (double) this.func_148135_f() ? (int) this.amountScrolled - this.func_148135_f() : 0), (double) height * 0.75D));
            int minY = Math.min(Math.max(this.getAmountScrolled() * (this.bottom - this.top - height) / maxScroll + this.top, this.top), this.bottom - height);
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollbarPositionMinX, this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            buffer.pos(scrollbarPositionMaxX, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            buffer.pos(scrollbarPositionMaxX, this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            buffer.pos(scrollbarPositionMinX, this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollbarPositionMinX, minY + height, 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            buffer.pos(scrollbarPositionMaxX, minY + height, 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            buffer.pos(scrollbarPositionMaxX, minY, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            buffer.pos(scrollbarPositionMinX, minY, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            tessellator.draw();
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(scrollbarPositionMinX, minY + height - 1, 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            buffer.pos(scrollbarPositionMaxX - 1, minY + height - 1, 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            buffer.pos(scrollbarPositionMaxX - 1, minY, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            buffer.pos(scrollbarPositionMinX, minY, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
        }
        this.func_148142_b(int_1, int_2);
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        callbackInfo.cancel();
    }
    
}
