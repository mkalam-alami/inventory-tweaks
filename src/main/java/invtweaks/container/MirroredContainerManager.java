package invtweaks.container;

import invtweaks.InvTweaks;
import invtweaks.InvTweaksConst;
import invtweaks.InvTweaksObfuscation;
import invtweaks.api.container.ContainerSection;
import invtweaks.forge.InvTweaksMod;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MirroredContainerManager implements IContainerManager {
    private ItemStack[] slotItems;
    private ItemStack heldItem;
    private List<ItemStack> droppedItems = new ArrayList<>();
    private Container container;
    private Map<ContainerSection, List<Integer>> itemRefs;
    private Map<ContainerSection, List<Slot>> slotRefs;
    private int clickDelay = 0;

    public MirroredContainerManager(Container cont) {
        container = cont;

        slotRefs = InvTweaksObfuscation.getContainerSlotMap(container);
        if(slotRefs == null) {
            slotRefs = new HashMap<>();
        }

        // TODO: Detect if there is a big enough unassigned section for inventory.
        @SuppressWarnings("unchecked")
        List<Slot> slots = (List<Slot>) container.inventorySlots;
        int size = slots.size();
        if(size >= InvTweaksConst.INVENTORY_SIZE && !slotRefs.containsKey(ContainerSection.INVENTORY)) {
            slotRefs.put(ContainerSection.INVENTORY, slots.subList(size - InvTweaksConst.INVENTORY_SIZE, size));
            slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR,
                    slots.subList(size - InvTweaksConst.INVENTORY_SIZE, size - HOTBAR_SIZE));
            slotRefs.put(ContainerSection.INVENTORY_HOTBAR, slots.subList(size - HOTBAR_SIZE, size));
        }

        itemRefs = new HashMap<>();
        for(Map.Entry<ContainerSection, List<Slot>> section : slotRefs.entrySet()) {
            List<Integer> slotIndices = new ArrayList<>(section.getValue().size());
            for(Slot slot : section.getValue()) {
                slotIndices.add(slots.indexOf(slot));
            }

            itemRefs.put(section.getKey(), slotIndices);
        }

        slotItems = new ItemStack[size];
        for(int i = 0; i < size; ++i) {
            slotItems[i] = slots.get(i).getStack();
        }

        heldItem = InvTweaks.getInstance().getHeldStack();
    }

    @Override
    public boolean move(ContainerSection srcSection, int srcIndex, ContainerSection destSection, int destIndex) {
        int srcSlotIdx = slotPositionToIndex(srcSection, srcIndex);

        if(destIndex == DROP_SLOT) {
            droppedItems.add(slotItems[srcSlotIdx]);
            slotItems[srcSlotIdx] = null;
        }

        int destSlotIdx = slotPositionToIndex(destSection, destIndex);

        Slot srcSlot = getSlot(srcSection, srcIndex);
        Slot destSlot = getSlot(destSection, destIndex);

        ItemStack srcItem = slotItems[srcSlotIdx];
        ItemStack destItem = slotItems[destSlotIdx];

        if(srcItem != null && !destSlot.isItemValid(srcItem)) {
            return false;
        }

        if(destItem != null && !srcSlot.isItemValid(destItem)) {
            // TODO: Behavior says move dest to empty valid slot in this case.
            return false;
        }

        // TODO: Attempt to stack instead of always swapping? [will need to actually copy stacks in init if so, also makes applying to actual inventory harder]
        slotItems[srcSlotIdx] = destItem;
        slotItems[destSlotIdx] = srcItem;

        return true;
    }

    @Override
    public boolean moveSome(ContainerSection srcSection, int srcIndex, ContainerSection destSection, int destIndex, int amount) {
        // TODO: Can't currently do partial movements
        return false;
    }

    @Override
    public boolean putHoldItemDown(ContainerSection destSection, int destIndex) {
        int destSlotIdx = slotPositionToIndex(destSection, destIndex);

        if(slotItems[destSlotIdx] != null) {
            return false;
        }

        slotItems[destSlotIdx] = heldItem;
        heldItem = null;

        return true;
    }

    @Override
    public void click(ContainerSection section, int index, boolean rightClick) {
        // TODO: Currently unused externally -- consider if it needs to really be in the interface.
    }

    @Override
    public boolean hasSection(ContainerSection section) {
        return itemRefs.containsKey(section);
    }

    @Override
    public List<Slot> getSlots(ContainerSection section) {
        return slotRefs.get(section);
    }

    @Override
    public int getSize() {
        return slotItems.length;
    }

    @Override
    public int getSize(ContainerSection section) {
        return itemRefs.get(section).size();
    }

    @Override
    public int getFirstEmptyIndex(ContainerSection section) {
        int i = 0;
        for(int slot : itemRefs.get(section)) {
            if(slotItems[slot] == null) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public boolean isSlotEmpty(ContainerSection section, int slot) {
        return getItemStack(section, slot) == null;
    }

    @Override
    public Slot getSlot(ContainerSection section, int index) {
        return getContainer().getSlot(slotPositionToIndex(section, index));
    }

    @Override
    public int getSlotIndex(int slotNumber, boolean preferInventory) {
        // TODO Caching with getSlotSection
        for(ContainerSection section : slotRefs.keySet()) {
            if(!preferInventory && section != ContainerSection.INVENTORY || (preferInventory && section != ContainerSection.INVENTORY_NOT_HOTBAR && section != ContainerSection.INVENTORY_HOTBAR)) {
                int i = 0;
                for(Slot slot : slotRefs.get(section)) {
                    if(InvTweaksObfuscation.getSlotNumber(slot) == slotNumber) {
                        return i;
                    }
                    i++;
                }
            }
        }
        return -1;
    }

    @Override
    public ContainerSection getSlotSection(int slotNumber) {
        // TODO Caching with getSlotIndex
        for(ContainerSection section : slotRefs.keySet()) {
            if(section != ContainerSection.INVENTORY) {
                for(Slot slot : slotRefs.get(section)) {
                    if(InvTweaksObfuscation.getSlotNumber(slot) == slotNumber) {
                        return section;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ItemStack getItemStack(ContainerSection section, int index) {
        return slotItems[slotPositionToIndex(section, index)];
    }

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void setClickDelay(int delay) {
        clickDelay = delay;
    }

    @Override
    public void applyChanges() {
        // TODO: Figure out what is needed to match container with virtual inventory.
        InvTweaksMod.proxy.sortComplete();
    }
    /**
     * Converts section/index values to slot ID.
     *
     * @param section
     * @param index
     * @return -1 if not found
     */
    private int slotPositionToIndex(ContainerSection section, int index) {
        if(index == DROP_SLOT) {
            return DROP_SLOT;
        } else if(index < 0) {
            return -1;
        } else if(hasSection(section)) {
            return itemRefs.get(section).get(index);
        } else {
            return -1;
        }
    }
}
