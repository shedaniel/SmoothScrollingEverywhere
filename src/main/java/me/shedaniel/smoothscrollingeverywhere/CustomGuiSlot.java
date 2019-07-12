package me.shedaniel.smoothscrollingeverywhere;

import me.shedaniel.smoothscrollingeverywhere.api.RunSixtyTimesEverySec;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CustomGuiSlot {
    
    public static void setScroller(GuiSlot list) {
        try {
            if (list.getClass().getField("smoothscrollingeverywhere_scroller").get(list) == null)
                list.getClass().getField("smoothscrollingeverywhere_scroller").set(list, new GuiSlotScroller(list));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    
    public static void setScrollAmount(GuiSlot list) {
        setScroller(list);
        try {
            list.getClass().getField("smoothscrollingeverywhere_scrollVelocity").setDouble(list, 0);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    
    public static void renderScrollbar(GuiSlot list, int mouseX, int mouseY) {
        try {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            int scrollbarPositionMinX = 0;
            try {
                Method getScrollbarPosition = GuiSlot.class.getDeclaredMethod("func_148137_d");
                getScrollbarPosition.setAccessible(true);
                scrollbarPositionMinX = (int) getScrollbarPosition.invoke(list);
            } catch (Exception e) {
                try {
                    Method getScrollbarPosition = GuiSlot.class.getDeclaredMethod("getScrollBarX");
                    getScrollbarPosition.setAccessible(true);
                    scrollbarPositionMinX = (int) getScrollbarPosition.invoke(list);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
            int maxScroll = list.getMaxScroll();
            
            int contentHeight = 0;
            try {
                Method getContentHeight = GuiSlot.class.getDeclaredMethod("func_148138_e");
                getContentHeight.setAccessible(true);
                contentHeight = (int) getContentHeight.invoke(list);
            } catch (Exception e) {
                try {
                    Method getContentHeight = GuiSlot.class.getDeclaredMethod("getContentHeight");
                    getContentHeight.setAccessible(true);
                    contentHeight = (int) getContentHeight.invoke(list);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            
            if (maxScroll > 0) {
                int height = (list.bottom - list.top) * (list.bottom - list.top) / contentHeight;
                height = MathHelper.clamp(height, 32, list.bottom - list.top - 8);
                height = (int) ((double) height - Math.min((double) (list.amountScrolled < 0.0D ? (int) (-list.amountScrolled) : (list.amountScrolled > (double) list.getMaxScroll() ? (int) list.amountScrolled - list.getMaxScroll() : 0)), (double) height * 0.75D));
                int minY = Math.min(Math.max((int) list.getAmountScrolled() * (list.bottom - list.top - height) / maxScroll + list.top, list.top), list.bottom - height);
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos((double) scrollbarPositionMinX, (double) list.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) scrollbarPositionMaxX, (double) list.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) scrollbarPositionMaxX, (double) list.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) scrollbarPositionMinX, (double) list.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
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
            try {
                Method renderDecorations = GuiSlot.class.getDeclaredMethod("func_148142_b", int.class, int.class);
                renderDecorations.setAccessible(true);
                renderDecorations.invoke(list, mouseX, mouseY);
            } catch (Exception e) {
                try {
                    Method renderDecorations = GuiSlot.class.getDeclaredMethod("renderDecorations", int.class, int.class);
                    renderDecorations.setAccessible(true);
                    renderDecorations.invoke(list, mouseX, mouseY);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlphaTest();
            GlStateManager.disableBlend();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void mouseScrolled(GuiSlot list, double amount) {
        setScroller(list);
        try {
            Field scrollVelocity = list.getClass().getField("smoothscrollingeverywhere_scrollVelocity");
            Field scroller = list.getClass().getField("smoothscrollingeverywhere_scroller");
            double velo = (double) scrollVelocity.get(list);
            RunSixtyTimesEverySec sec = (RunSixtyTimesEverySec) scroller.get(list);
            if (list.amountScrolled <= list.getMaxScroll() && amount < 0.0D)
                velo += 16;
            if (list.amountScrolled >= 0.0D && amount > 0.0D)
                velo -= 16;
            scrollVelocity.setDouble(list, velo);
            if (!sec.isRegistered())
                sec.registerTick();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
