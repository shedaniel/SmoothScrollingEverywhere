package me.shedaniel.smoothscrollingeverywhere;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.smoothscrollingeverywhere.api.RunSixtyTimesEverySec;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CustomAbstractList {
    
    public static void setScroller(AbstractList list) {
        try {
            if (list.getClass().getField("smoothscrollingeverywhere_scroller").get(list) == null)
                list.getClass().getField("smoothscrollingeverywhere_scroller").set(list, new AbstractListScroller(list));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    
    public static void setScrollAmount(AbstractList list) {
        setScroller(list);
        try {
            list.getClass().getField("smoothscrollingeverywhere_scrollVelocity").setDouble(list, 0);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    
    public static void renderScrollbar(AbstractList list, int mouseX, int mouseY) {
        try {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            Method getScrollbarPosition = AbstractList.class.getDeclaredMethod("getScrollbarPosition");
            getScrollbarPosition.setAccessible(true);
            int scrollbarPositionMinX = (int) getScrollbarPosition.invoke(list);
            int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
            int maxScroll = list.getMaxScroll();
            if (maxScroll > 0) {
                int height = (list.getBottom() - list.getTop()) * (list.getBottom() - list.getTop()) / list.getMaxPosition();
                height = MathHelper.clamp(height, 32, list.getBottom() - list.getTop() - 8);
                height = (int) ((double) height - Math.min((double) (list.scrollAmount < 0.0D ? (int) (-list.scrollAmount) : (list.scrollAmount > (double) list.getMaxScroll() ? (int) list.scrollAmount - list.getMaxScroll() : 0)), (double) height * 0.75D));
                int minY = Math.min(Math.max((int) list.getScrollAmount() * (list.getBottom() - list.getTop() - height) / maxScroll + list.getTop(), list.getTop()), list.getBottom() - height);
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos((double) scrollbarPositionMinX, (double) list.getBottom(), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) scrollbarPositionMaxX, (double) list.getBottom(), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) scrollbarPositionMaxX, (double) list.getTop(), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) scrollbarPositionMinX, (double) list.getTop(), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
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
            Method renderDecorations = AbstractList.class.getDeclaredMethod("renderDecorations", int.class, int.class);
            renderDecorations.setAccessible(true);
            renderDecorations.invoke(list, mouseX, mouseY);
            GlStateManager.enableTexture();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlphaTest();
            GlStateManager.disableBlend();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    
    public static void mouseScrolled(AbstractList list, double amount) {
        setScroller(list);
        try {
            Field scrollVelocity = list.getClass().getField("smoothscrollingeverywhere_scrollVelocity");
            Field scroller = list.getClass().getField("smoothscrollingeverywhere_scroller");
            double velo = (double) scrollVelocity.get(list);
            RunSixtyTimesEverySec sec = (RunSixtyTimesEverySec) scroller.get(list);
            if (list.scrollAmount <= list.getMaxScroll() && amount < 0.0D)
                velo += 16;
            if (list.scrollAmount >= 0.0D && amount > 0.0D)
                velo -= 16;
            scrollVelocity.setDouble(list, velo);
            if (!sec.isRegistered())
                sec.registerTick();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
