package me.shedaniel.smoothscrollingeverywhere;

import me.shedaniel.clothconfig2.api.ScrollingContainer;
import me.shedaniel.math.Rectangle;
import me.shedaniel.smoothscrollingeverywhere.mixin.EntryListWidgetAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.util.Lazy;
import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public class EntryListWidgetScroller extends ScrollingContainer {
    private final EntryListWidget widget;
    private final EntryListWidgetAccessor accessor;
    private static final Lazy<Method> MAX_POS = new Lazy<>(() -> {
        try {
            Method method = EntryListWidget.class.getDeclaredMethod(FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", "net.minecraft.class_350", "method_25317", "()I"));
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });
    private static final Lazy<Method> SCROLLBAR_X = new Lazy<>(() -> {
        try {
            Method method = EntryListWidget.class.getDeclaredMethod(FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", "net.minecraft.class_350", "method_25329", "()I"));
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });
    
    public EntryListWidgetScroller(EntryListWidget widget) {
        this.widget = widget;
        this.accessor = (EntryListWidgetAccessor) widget;
    }
    
    @Override
    public Rectangle getBounds() {
        return new Rectangle(accessor.getLeft(), accessor.getTop(), accessor.getRight() - accessor.getLeft(), accessor.getBottom() - accessor.getTop());
    }
    
    @Override
    public int getMaxScrollHeight() {
        try {
            return (int) MAX_POS.get().invoke(widget);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public int getScrollBarX() {
        try {
            return (int) SCROLLBAR_X.get().invoke(widget);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
