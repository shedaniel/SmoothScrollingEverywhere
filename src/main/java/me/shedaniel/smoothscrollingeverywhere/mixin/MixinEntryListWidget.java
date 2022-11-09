package me.shedaniel.smoothscrollingeverywhere.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScrollingContainer;
import me.shedaniel.smoothscrollingeverywhere.EntryListWidgetScroller;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
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
    protected abstract void renderDecorations(MatrixStack stack, int int_1, int int_2);

    @Shadow protected int bottom;

    @Shadow protected int top;

    @Shadow protected abstract int getScrollbarPositionX();

    @Shadow public abstract int getMaxScroll();

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
    public void render(MatrixStack stack, int int_1, int int_2, float delta, CallbackInfo callbackInfo) {
        scroller.updatePosition(delta);
        this.scrollAmount = scroller.scrollAmount;
    }

    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/EntryListWidget;getMaxScroll()I", ordinal = 0, shift = At.Shift.AFTER),
            cancellable = true)
    public void renderScrollbar(MatrixStack stack, int int_1, int int_2, float float_1, CallbackInfo callbackInfo) {
        // Render Black Background
        if (this.getMaxScroll() > 0) {
            final Tessellator tessellator = Tessellator.getInstance();
            final BufferBuilder bufferBuilder = tessellator.getBuffer();

            final int i = this.getScrollbarPositionX();
            final int j = i + 6;

            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            bufferBuilder.vertex(i, this.bottom, 0.0).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(j, this.bottom, 0.0).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(j, this.top, 0.0).color(0, 0, 0, 255).next();
            bufferBuilder.vertex(i, this.top, 0.0).color(0, 0, 0, 255).next();

            tessellator.draw();
        }

        scroller.renderScrollBar();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        this.renderDecorations(stack, int_1, int_2);
        callbackInfo.cancel();
    }
}
