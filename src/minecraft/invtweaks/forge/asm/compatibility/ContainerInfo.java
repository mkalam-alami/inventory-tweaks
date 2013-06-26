package invtweaks.forge.asm.compatibility;

import invtweaks.forge.asm.ContainerTransformer;

@SuppressWarnings("UnusedDeclaration")
public class ContainerInfo {
    public boolean standardInventory = false;
    public boolean validInventory = false;
    public boolean validChest = false;
    public boolean largeChest = false;
    public short rowSize = 9;
    public MethodInfo slotMapMethod = ContainerTransformer.getVanillaSlotMapInfo("unknownContainerSlots");
    public MethodInfo rowSizeMethod = null;

    public ContainerInfo() {
    }

    public ContainerInfo(boolean standard, boolean validInv, boolean validCh) {
        standardInventory = standard;
        validInventory = validInv;
        validChest = validCh;
    }

    public ContainerInfo(boolean standard, boolean validInv, boolean validCh, boolean largeCh) {
        standardInventory = standard;
        validInventory = validInv;
        validChest = validCh;
        largeChest = largeCh;
    }

    public ContainerInfo(boolean standard, boolean validInv, boolean validCh, MethodInfo slotMap) {
        standardInventory = standard;
        validInventory = validInv;
        validChest = validCh;
        slotMapMethod = slotMap;
    }

    public ContainerInfo(boolean standard, boolean validInv, boolean validCh, short rowS) {
        standardInventory = standard;
        validInventory = validInv;
        validChest = validCh;
        rowSize = rowS;
    }

    public ContainerInfo(boolean standard, boolean validInv, boolean validCh, boolean largeCh, short rowS) {
        standardInventory = standard;
        validInventory = validInv;
        validChest = validCh;
        largeChest = largeCh;
        rowSize = rowS;
    }

    public ContainerInfo(boolean standard, boolean validInv, boolean validCh, short rowS,
                         MethodInfo slotMap) {
        standardInventory = standard;
        validInventory = validInv;
        validChest = validCh;
        rowSize = rowS;
        slotMapMethod = slotMap;
    }
}
