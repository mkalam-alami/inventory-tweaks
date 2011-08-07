package net.invtweaks.framework;

import java.util.List;
import java.util.concurrent.TimeoutException;

import net.invtweaks.framework.ContainerManager.ContainerSection;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Container;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;


/**
 * Allows to perform various operations on a single section of
 * the inventory and/or containers. Works in both single and multiplayer.
 * 
 * @author Jimeo Wan
 *
 */
public class ContainerSectionManager {

    private ContainerManager containerMgr;
    private ContainerSection section;
    
    public ContainerSectionManager(Minecraft mc, ContainerSection section) throws Exception {
        this.containerMgr = new ContainerManager(mc);
        this.section = section;
        
        if (!containerMgr.isSectionAvailable(section)) {
            throw new Exception("Section not available");
        }
    }

    public boolean move(int srcIndex, int destIndex) throws TimeoutException {
        return containerMgr.move(section, srcIndex, section, destIndex);
    }

    public boolean moveSome(int srcIndex, int destIndex, int amount) throws TimeoutException {
        return containerMgr.moveSome(section, srcIndex, section, destIndex, amount);
    }

    public void leftClick(int index) throws TimeoutException {
        containerMgr.leftClick(section, index);
    }

    public void rightClick(int index) throws TimeoutException {
        containerMgr.rightClick(section, index);
    }

    public void click(int index, boolean rightClick) throws TimeoutException {
        containerMgr.click(section, index, rightClick);
    }

    public List<Slot> getSlots() {
        return containerMgr.getSectionSlots(section);
    }

    public int getSectionSize() {
        return containerMgr.getSectionSize(section);
    }

    public boolean isSlotEmpty(int slot) {
        return containerMgr.isSlotEmpty(section, slot);
    }

    public ItemStack getItemStack(int index) throws NullPointerException, IndexOutOfBoundsException {
        return containerMgr.getItemStack(section, index);
    }

    public Container getContainer() {
        return containerMgr.getContainer();
    }
    
}
