package me.shedaniel.smoothscrollingeverywhere;

import me.shedaniel.smoothscrollingeverywhere.api.RunSixtyTimesEverySec;
import net.minecraft.client.gui.widget.list.AbstractList;

import java.lang.reflect.Field;

public class AbstractListScroller implements RunSixtyTimesEverySec {
    
    private AbstractList list;
    
    public AbstractListScroller(AbstractList list) {
        this.list = list;
    }
    
    @Override
    public void run() {
        if (list == null) {
            System.err.println("LIST IS GONE!");
            return;
        }
        try {
            Field scrollVelocity = list.getClass().getField("smoothscrollingeverywhere_scrollVelocity");
            if ((double) scrollVelocity.get(list) == 0.0D && list.scrollAmount >= 0.0D && list.scrollAmount <= list.getMaxScroll()) {
                unregisterTick();
            } else {
                double change = ((double) scrollVelocity.get(list)) * 0.3D;
                if ((double) scrollVelocity.get(list) != 0.0D) {
                    list.scrollAmount += change;
                    double minus = ((double) scrollVelocity.get(list)) * (list.scrollAmount >= 0.0D && list.scrollAmount <= list.getMaxScroll() ? 0.2D : 0.4D);
                    scrollVelocity.setDouble(list, ((double) scrollVelocity.get(list)) - minus);
                    if (Math.abs((double) scrollVelocity.get(list)) < 0.1D) {
                        scrollVelocity.setDouble(list, 0);
                    }
                }
                
                if (list.scrollAmount < 0.0D && (double) scrollVelocity.get(list) == 0.0D) {
                    list.scrollAmount = Math.min(list.scrollAmount + (0.0D - list.scrollAmount) * 0.2D, 0.0D);
                    if (list.scrollAmount > -0.1D && list.scrollAmount < 0.0D) {
                        list.scrollAmount = 0.0D;
                    }
                } else if (list.scrollAmount > list.getMaxScroll() && (double) scrollVelocity.get(list) == 0.0D) {
                    list.scrollAmount = Math.max(list.scrollAmount - (list.scrollAmount - list.getMaxScroll()) * 0.2D, list.getMaxScroll());
                    if (list.scrollAmount > list.getMaxScroll() && list.scrollAmount < list.getMaxScroll() + 0.1D) {
                        list.scrollAmount = list.getMaxScroll();
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
