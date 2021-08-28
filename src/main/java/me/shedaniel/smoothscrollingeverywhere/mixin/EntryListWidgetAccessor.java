package me.shedaniel.smoothscrollingeverywhere.mixin;

import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntryListWidget.class)
public interface EntryListWidgetAccessor {
    @Accessor("left")
    int getLeft();
    
    @Accessor("right")
    int getRight();
    
    @Accessor("top")
    int getTop();
    
    @Accessor("bottom")
    int getBottom();
}
