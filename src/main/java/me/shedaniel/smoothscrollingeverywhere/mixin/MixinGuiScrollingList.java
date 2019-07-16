package me.shedaniel.smoothscrollingeverywhere.mixin;

import me.shedaniel.smoothscrollingeverywhere.api.RunSixtyTimesEverySec;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
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
    
    @Shadow(remap = false) @Final protected int bottom;
    @Shadow(remap = false) @Final protected int top;
    @Unique protected double scrollVelocity;
    @Shadow(remap = false) @Final protected int left;
    @Shadow(remap = false) @Final protected int listWidth;
    @Shadow(remap = false) private float scrollDistance;
    @Unique protected RunSixtyTimesEverySec scroller = () -> {
        if (this.scrollVelocity == 0.0D && this.scrollDistance >= 0.0D && this.scrollDistance <= this.func_148135_f()) {
            this.scrollerUnregisterTick();
        } else {
            double change = this.scrollVelocity * 0.3D;
            if (this.scrollVelocity != 0.0D) {
                this.scrollDistance += change;
                this.scrollVelocity -= this.scrollVelocity * (this.scrollDistance >= 0.0D && this.scrollDistance <= this.func_148135_f() ? 0.2D : 0.4D);
                if (Math.abs(this.scrollVelocity) < 0.1D) {
                    this.scrollVelocity = 0.0D;
                }
            }
            if (this.scrollDistance < 0.0f && this.scrollVelocity == 0.0D) {
                this.scrollDistance = Math.min(this.scrollDistance + (0.0f - this.scrollDistance) * 0.2f, 0.0f);
                if (this.scrollDistance > -0.1f && this.scrollDistance < 0.0f) {
                    this.scrollDistance = 0.0f;
                }
            } else if (this.scrollDistance > this.func_148135_f() && this.scrollVelocity == 0.0D) {
                this.scrollDistance = Math.max(this.scrollDistance - (this.scrollDistance - this.func_148135_f()) * 0.2f, this.func_148135_f());
                if (this.scrollDistance > this.func_148135_f() && this.scrollDistance < this.func_148135_f() + 0.1D) {
                    this.scrollDistance = this.func_148135_f();
                }
            }
        }
    };
    
    @Shadow(remap = false)
    protected abstract int getContentHeight();
    
    @Shadow(remap = false)
    protected abstract void drawScreen(int mouseX, int mouseY);
    
    @Shadow
    protected abstract void applyScrollLimits();
    
    @Unique
    private int func_148135_f() {
        return Math.max(this.getContentHeight() - (this.bottom - this.top) - 4, 0);
    }
    
    @Unique
    private void scrollerUnregisterTick() {
        this.scroller.unregisterTick();
    }
    
    @Redirect(method = "drawScreen(IIF)V",
              at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I", ordinal = 0,
                       remap = false), remap = false)
    public int getEventDWheel(int mouseX, int mouseY, float partialTicks) {
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int scroll = 0;
            if (wheel > 0)
                scroll = 1;
            else if (wheel < 0)
                scroll = -1;
            if (this.scrollDistance <= this.func_148135_f() && scroll < 0.0D)
                this.scrollVelocity += 16.0D;
            if (this.scrollDistance >= 0.0D && scroll > 0.0D)
                this.scrollVelocity -= 16.0D;
            if (!this.scroller.isRegistered())
                this.scroller.registerTick();
        }
        return 0;
    }
    
    @Redirect(method = "drawScreen(IIF)V", at = @At(value = "INVOKE",
                                                    target = "Lnet/minecraftforge/fml/client/GuiScrollingList;applyScrollLimits()V",
                                                    remap = false), remap = false)
    public void bindScrollDistance(GuiScrollingList guiSlot) {
        if (Mouse.isButtonDown(0))
            applyScrollLimits();
    }
    
    @Inject(method = "drawScreen(IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/GuiScrollingList;getContentHeight()I",
                     ordinal = 2, shift = At.Shift.AFTER, remap = false), cancellable = true, remap = false)
    public void render(int int_1, int int_2, float float_1, CallbackInfo callbackInfo) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer buffer = tessellator.getWorldRenderer();
        int scrollbarPositionMaxX = this.left + this.listWidth;
        int scrollbarPositionMinX = scrollbarPositionMaxX - 6;
        int maxScroll = this.func_148135_f();
        if (maxScroll > 0) {
            int height = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
            height = MathHelper.clamp_int(height, 32, this.bottom - this.top - 8);
            height = (int) ((double) height - Math.min((double) (this.scrollDistance < 0.0D ? (int) (-this.scrollDistance) : (this.scrollDistance > (double) this.func_148135_f() ? (int) this.scrollDistance - this.func_148135_f() : 0)), (double) height * 0.75D));
            int minY = Math.min(Math.max(((int) scrollDistance) * (this.bottom - this.top - height) / maxScroll + this.top, this.top), this.bottom - height);
            GlStateManager.disableTexture2D();
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
