package net.invtweaks.library;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.Slot;

public class GuiContainerHelper extends GuiContainer {

    public GuiContainerHelper(Minecraft mc) throws ClassCastException {
        super(((GuiContainer) mc.currentScreen).inventorySlots);
    }

    public Slot getSlotAtPosition(int i, int j) {
        for (int k = 0; k < inventorySlots.slots.size(); k++) {
            Slot slot = (Slot)inventorySlots.slots.get(k);
            if (getIsMouseOverSlot(slot, i, j)) {
                return slot;
            }
        }
        return null;
    }

    private boolean getIsMouseOverSlot(Slot slot, int i, int j) {
        
        int k = (width - xSize) / 2;
        int l = (height - ySize) / 2;
        i -= k;
        j -= l;
        return i >= slot.xDisplayPosition - 1 && i < slot.xDisplayPosition + 16 + 1 && j >= slot.yDisplayPosition - 1 && j < slot.yDisplayPosition + 16 + 1;
    }

    protected void drawGuiContainerBackgroundLayer(float f) {
        // Do nothing
    }
    
}
