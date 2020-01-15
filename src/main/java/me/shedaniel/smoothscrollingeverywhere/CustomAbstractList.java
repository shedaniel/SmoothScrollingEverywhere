package me.shedaniel.smoothscrollingeverywhere;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public class CustomAbstractList {
    
    public static void setTarget(AbstractList list, double target) {
        try {
            list.getClass().getField("smoothscrollingeverywhere_target").set(list, target);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    
    public static double getTarget(AbstractList list) {
        try {
            return (double) list.getClass().getField("smoothscrollingeverywhere_target").get(list);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return list.scrollAmount;
    }
    
    public static void setStart(AbstractList list, long start) {
        try {
            list.getClass().getField("smoothscrollingeverywhere_start").set(list, start);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    
    public static long getStart(AbstractList list) {
        try {
            return (long) list.getClass().getField("smoothscrollingeverywhere_start").get(list);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public static void setDuration(AbstractList list, long duration) {
        try {
            list.getClass().getField("smoothscrollingeverywhere_duration").set(list, duration);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    
    public static long getDuration(AbstractList list) {
        try {
            return (long) list.getClass().getField("smoothscrollingeverywhere_duration").get(list);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public static void clamp(AbstractList list, double scroll) {
        scrollTo(list, clamp(scroll, list.getMaxScroll(), 0), false);
    }
    
    public static EasingMethod getEasingMethod() {
        return v -> v;
    }
    
    public static long getScrollDuration() {
        return 800;
    }
    
    public static double getScrollStep() {
        return 19;
    }
    
    public static double getBounceBackMultiplier() {
        return .24;
    }
    
    public static double handleScrollingPosition(double[] target, double scroll, double maxScroll, float delta, double start, double duration) {
        if (getBounceBackMultiplier() >= 0) {
            target[0] = clamp(target[0], maxScroll);
            if (target[0] < 0) {
                target[0] -= target[0] * (1 - getBounceBackMultiplier()) * delta / 3;
            } else if (target[0] > maxScroll) {
                target[0] = (target[0] - maxScroll) * (1 - (1 - getBounceBackMultiplier()) * delta / 3) + maxScroll;
            }
        } else
            target[0] = clamp(target[0], maxScroll, 0);
        if (!Precision.almostEquals(scroll, target[0], Precision.FLOAT_EPSILON))
            return expoEase(scroll, target[0], Math.min((System.currentTimeMillis() - start) / duration, 1));
        else
            return target[0];
    }
    
    public static double expoEase(double start, double end, double amount) {
        return start + (end - start) * getEasingMethod().apply(amount);
    }
    
    public static class Precision {
        public static final float FLOAT_EPSILON = 1e-3f;
        public static final double DOUBLE_EPSILON = 1e-7;
        
        public static boolean almostEquals(float value1, float value2, float acceptableDifference) {
            return Math.abs(value1 - value2) <= acceptableDifference;
        }
        
        public static boolean almostEquals(double value1, double value2, double acceptableDifference) {
            return Math.abs(value1 - value2) <= acceptableDifference;
        }
    }
    
    public static double clamp(double v, double maxScroll) {
        return clamp(v, maxScroll, 300);
    }
    
    public static double clamp(double v, double maxScroll, double clampExtension) {
        return MathHelper.clamp(v, -clampExtension, maxScroll + clampExtension);
    }
    
    public static void offset(AbstractList list, double value, boolean animated) {
        scrollTo(list, getTarget(list) + value, animated);
    }
    
    public static void scrollTo(AbstractList list, double value, boolean animated) {
        scrollTo(list, value, animated, getScrollDuration());
    }
    
    public static void scrollTo(AbstractList list, double value, boolean animated, long duration) {
        setTarget(list, clamp(value, list.getMaxScroll()));
        
        if (animated) {
            setStart(list, System.currentTimeMillis());
            setDuration(list, duration);
        } else
            list.scrollAmount = getTarget(list);
    }
    
    public static void updatePosition(AbstractList list, float delta) {
        double[] target = new double[]{getTarget(list)};
        list.scrollAmount = handleScrollingPosition(target, list.scrollAmount, list.getMaxScroll(), delta, getStart(list), getDuration(list));
        setTarget(list, target[0]);
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
                height = (int) ((double) height - Math.min(list.scrollAmount < 0.0D ? (int) (-list.scrollAmount) : (list.scrollAmount > (double) list.getMaxScroll() ? (int) list.scrollAmount - list.getMaxScroll() : 0), (double) height * 0.75D));
                int minY = Math.min(Math.max((int) list.getScrollAmount() * (list.getBottom() - list.getTop() - height) / maxScroll + list.getTop(), list.getTop()), list.getBottom() - height);
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.func_225582_a_(scrollbarPositionMinX, list.getBottom(), 0.0D).func_225583_a_(0.0F, 1.0F).func_225586_a_(0, 0, 0, 255).endVertex();
                buffer.func_225582_a_(scrollbarPositionMaxX, list.getBottom(), 0.0D).func_225583_a_(1.0F, 1.0F).func_225586_a_(0, 0, 0, 255).endVertex();
                buffer.func_225582_a_(scrollbarPositionMaxX, list.getTop(), 0.0D).func_225583_a_(1.0F, 0.0F).func_225586_a_(0, 0, 0, 255).endVertex();
                buffer.func_225582_a_(scrollbarPositionMinX, list.getTop(), 0.0D).func_225583_a_(0.0F, 0.0F).func_225586_a_(0, 0, 0, 255).endVertex();
                tessellator.draw();
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.func_225582_a_(scrollbarPositionMinX, minY + height, 0.0D).func_225583_a_(0.0F, 1.0F).func_225586_a_(128, 128, 128, 255).endVertex();
                buffer.func_225582_a_(scrollbarPositionMaxX, minY + height, 0.0D).func_225583_a_(1.0F, 1.0F).func_225586_a_(128, 128, 128, 255).endVertex();
                buffer.func_225582_a_(scrollbarPositionMaxX, minY, 0.0D).func_225583_a_(1.0F, 0.0F).func_225586_a_(128, 128, 128, 255).endVertex();
                buffer.func_225582_a_(scrollbarPositionMinX, minY, 0.0D).func_225583_a_(0.0F, 0.0F).func_225586_a_(128, 128, 128, 255).endVertex();
                tessellator.draw();
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.func_225582_a_(scrollbarPositionMinX, minY + height - 1, 0.0D).func_225583_a_(0.0F, 1.0F).func_225586_a_(192, 192, 192, 255).endVertex();
                buffer.func_225582_a_(scrollbarPositionMaxX - 1, minY + height - 1, 0.0D).func_225583_a_(1.0F, 1.0F).func_225586_a_(192, 192, 192, 255).endVertex();
                buffer.func_225582_a_(scrollbarPositionMaxX - 1, minY, 0.0D).func_225583_a_(1.0F, 0.0F).func_225586_a_(192, 192, 192, 255).endVertex();
                buffer.func_225582_a_(scrollbarPositionMinX, minY, 0.0D).func_225583_a_(0.0F, 0.0F).func_225586_a_(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }
            Method renderDecorations = AbstractList.class.getDeclaredMethod("renderDecorations", int.class, int.class);
            renderDecorations.setAccessible(true);
            renderDecorations.invoke(list, mouseX, mouseY);
            RenderSystem.enableTexture();
            RenderSystem.shadeModel(7424);
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    
    public static void mouseScrolled(AbstractList list, double amount) {
        offset(list, getScrollStep() * -amount, true);
    }
}
