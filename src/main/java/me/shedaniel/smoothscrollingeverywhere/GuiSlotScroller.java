package me.shedaniel.smoothscrollingeverywhere;

import me.shedaniel.smoothscrollingeverywhere.api.RunSixtyTimesEverySec;
import net.minecraft.client.gui.GuiSlot;

import java.lang.reflect.Field;

public class GuiSlotScroller implements RunSixtyTimesEverySec {
    
    private GuiSlot list;
    
    public GuiSlotScroller(GuiSlot list) {
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
            if ((double) scrollVelocity.get(list) == 0.0D && list.amountScrolled >= 0.0D && list.amountScrolled <= list.getMaxScroll()) {
                unregisterTick();
            } else {
                double change = ((double) scrollVelocity.get(list)) * 0.3D;
                if ((double) scrollVelocity.get(list) != 0.0D) {
                    list.amountScrolled += change;
                    double minus = ((double) scrollVelocity.get(list)) * (list.amountScrolled >= 0.0D && list.amountScrolled <= list.getMaxScroll() ? 0.2D : 0.4D);
                    scrollVelocity.setDouble(list, ((double) scrollVelocity.get(list)) - minus);
                    if (Math.abs((double) scrollVelocity.get(list)) < 0.1D) {
                        scrollVelocity.setDouble(list, 0);
                    }
                }
                
                if (list.amountScrolled < 0.0D && (double) scrollVelocity.get(list) == 0.0D) {
                    list.amountScrolled = Math.min(list.amountScrolled + (0.0D - list.amountScrolled) * 0.2D, 0.0D);
                    if (list.amountScrolled > -0.1D && list.amountScrolled < 0.0D) {
                        list.amountScrolled = 0.0D;
                    }
                } else if (list.amountScrolled > list.getMaxScroll() && (double) scrollVelocity.get(list) == 0.0D) {
                    list.amountScrolled = Math.max(list.amountScrolled - (list.amountScrolled - list.getMaxScroll()) * 0.2D, list.getMaxScroll());
                    if (list.amountScrolled > list.getMaxScroll() && list.amountScrolled < list.getMaxScroll() + 0.1D) {
                        list.amountScrolled = list.getMaxScroll();
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
