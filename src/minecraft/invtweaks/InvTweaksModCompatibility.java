package invtweaks;

import invtweaks.api.ContainerGUI;
import invtweaks.api.ContainerSection;
import invtweaks.api.InventoryGUI;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InvTweaksModCompatibility {

    private final InvTweaksObfuscation obf;

    public InvTweaksModCompatibility(InvTweaksObfuscation obf) {
        this.obf = obf;
    }

    /**
     * Returns true if the screen is a chest/dispenser,
     * despite not being a GuiChest or a GuiDispenser.
     *
     * @param guiScreen
     */
    public boolean isSpecialChest(GuiScreen guiScreen) {
        return getContainerGUIAnnotation(guiScreen.getClass()) != null // API-marked classes
                || isExact(guiScreen, "cpw.mods.ironchest.client.GUIChest") // Iron chests (formerly IC2)
                || isExact(guiScreen, "cubex2.mods.multipagechest.client.GuiMultiPageChest") // Multi Page chest
                || isExact(guiScreen, "com.eloraam.redpower.machine.GuiBufferChest") // Red Power 2
                || isExact(guiScreen, "com.eloraam.redpower.machine.GuiRetriever") // Red Power 2
                || isExact(guiScreen, "com.eloraam.redpower.machine.GuiItemDetect") // Red Power 2
                || isExact(guiScreen, "com.eloraam.redpower.base.GuiAlloyFurnace") // Red Power 2
                || isExact(guiScreen, "com.eloraam.redpower.machine.GuiDeploy") // Red Power 2
                || isExact(guiScreen, "com.eloraam.redpower.machine.GuiSorter") // Red Power 2
                || isExact(guiScreen, "com.eloraam.redpower.machine.GuiFilter") // Red Power 2
                || isExact(guiScreen, "com.eloraam.redpower.base.GuiAdvBench") // Red Power 2
                || isExact(guiScreen, "com.eloraam.redpower.machine.GuiEject") // Red Power 2
                || isExact(guiScreen, "com.eloraam.redpower.base.GuiBag") // Red Power 2
                || isExact(guiScreen, "com.eloraam.redpower.world.GuiSeedBag") // Red Power 2
                || isExact(guiScreen, "ic2.core.block.personal.GuiPersonalChest")
                || isExact(guiScreen, "ic2.core.block.generator.gui.GuiNuclearReactor") // IC2
                || isExact(guiScreen, "mods.laco.colorbox.client.GuiColorBox")
                || isExact(guiScreen, "shadow.mods.metallurgy.precious.FC_GuiChest") // Metallurgy
                || isExact(guiScreen, "shadow.mods.metallurgy.precious.FM_GuiMintStorage") // Metallurgy
                || isExact(guiScreen, "TFC.GUI.GuiChestTFC") // TerraFirmaCraft
                || isExact(guiScreen, "forestry.storage.gui.GuiBackpack")
                || isExact(guiScreen, "forestry.storage.gui.GuiBackpackT2")
                || isExact(guiScreen, "com.pahimar.ee3.client.gui.inventory.GuiPortableCrafting")
                || isExact(guiScreen, "codechicken.enderstorage.storage.item.GuiEnderItemStorage")
                || isExact(guiScreen, "net.mcft.copy.betterstorage.client.GuiReinforcedChest")
                ;
    }

    /**
     * Returns a special chest row size.
     * Given guiContainer must be checked first with isSpecialChest().
     *
     * @param guiContainer
     * @param defaultValue
     */
    public int getSpecialChestRowSize(GuiContainer guiContainer, int defaultValue) {
        ContainerGUI annotation = getContainerGUIAnnotation(guiContainer.getClass());
        if (annotation != null) {
            Method m = getAnnotatedMethod(guiContainer.getClass(), new Class[]{ContainerGUI.RowSizeCallback.class}, 0, int.class);
            if (m != null) {
                try {
                    return (Integer) m.invoke(guiContainer);
                } catch (Exception e) {
                    // TODO: Do something here to tell mod authors they're doing it wrong.
                    return annotation.rowSize();
                }
            } else {
                return annotation.rowSize();
            }
        } else if (isExact(guiContainer, "cpw.mods.ironchest.client.GUIChest")) { // Iron chests (formerly IC2)
            try {
                return (Integer) guiContainer.getClass().getMethod("getRowLength").invoke(guiContainer);
            } catch (Exception e) {
                return defaultValue;
            }
        } else if (isExact(guiContainer, "cubex2.mods.multipagechest.client.GuiMultiPageChest")) { // Multi Page chest
            return 13;
        } else if (isExact(guiContainer, "com.eloraam.redpower.machine.GuiBufferChest")) { // Red Power 2
            return 4;
        } else if (isExact(guiContainer, "com.eloraam.redpower.machine.GuiSorter")) {
            return 8;
        } else if (isExact(guiContainer, "com.eloraam.redpower.machine.GuiRetriever")
                || isExact(guiContainer, "com.eloraam.redpower.machine.GuiItemDetect")
                || isExact(guiContainer, "com.eloraam.redpower.base.GuiAlloyFurnace")
                || isExact(guiContainer, "com.eloraam.redpower.machine.GuiDeploy")
                || isExact(guiContainer, "com.eloraam.redpower.machine.GuiFilter")
                || isExact(guiContainer, "com.eloraam.redpower.machine.GuiEject")) {
            return 3;
        } else if (isExact(guiContainer, "ic2.core.block.generator.gui.GuiNuclearReactor")) { // IC2
            return (obf.getSlots(obf.getContainer(guiContainer)).size() - 36) / 6;
        } else if (isExact(guiContainer, "net.mcft.copy.betterstorage.client.GuiReinforcedChest")) {
            try {
                return (Integer) guiContainer.getClass().getMethod("getNumColumns").invoke(guiContainer);
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public boolean isChestWayTooBig(GuiScreen guiScreen) {
        return isExact(guiScreen, "cubex2.mods.multipagechest.client.GuiMultiPageChest") // MultiPage Chest
                || isExact(guiScreen, "cpw.mods.ironchest.client.GUIChest") // IronChests
                || isExact(guiScreen, "shadow.mods.metallurgy.precious.FC_GuiChest") // Metallurgy
                ;
    }

    /**
     * Returns true if the screen is the inventory screen, despite not being a GuiInventory.
     *
     * @param guiScreen
     */
    public boolean isSpecialInventory(GuiScreen guiScreen) {
        if (getInventoryGUIAnnotation(guiScreen.getClass()) != null) {
            return true;
        } else if (isExact(guiScreen, "micdoodle8.mods.galacticraft.core.client.gui.GCCoreGuiTankRefill")) {
            return true;
        }
        try {
            return obf.getSlots(obf.getContainer(obf.asGuiContainer(guiScreen))).size() > InvTweaksConst.INVENTORY_SIZE
                    && !obf.isGuiInventoryCreative(guiScreen);
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<ContainerSection, List<Slot>> getSpecialContainerSlots(GuiScreen guiScreen, Container container) {
        Class<? extends GuiScreen> clazz = guiScreen.getClass();
        if (isAPIClass(clazz)) {
            Method m = getAnnotatedMethod(clazz, new Class[]{ContainerGUI.ContainerSectionCallback.class, InventoryGUI.ContainerSectionCallback.class}, 0, Map.class);
            if (m != null) {
                try {
                    return (Map<ContainerSection, List<Slot>>) m.invoke(guiScreen);
                } catch (Exception e) {
                    // TODO: Do something here to tell mod authors they're doing it wrong.
                }
            }
        }

        Map<ContainerSection, List<Slot>> result = new HashMap<ContainerSection, List<Slot>>();
        List<Slot> slots = (List<Slot>) obf.getSlots(container);

        if (isExact(guiScreen, "com.eloraam.redpower.base.GuiAdvBench")) { // RedPower 2
            result.put(ContainerSection.CRAFTING_IN_PERSISTENT, slots.subList(0, 9));
            result.put(ContainerSection.CRAFTING_OUT, slots.subList(10, 11));
            result.put(ContainerSection.CHEST, slots.subList(11, 29));
        } else if (isExact(guiScreen, "thaumcraft.client.gui.GuiArcaneWorkbench")
                || isExact(guiScreen, "thaumcraft.client.gui.GuiInfusionWorkbench")) { // Thaumcraft 3
            result.put(ContainerSection.CRAFTING_OUT, slots.subList(0, 1));
            result.put(ContainerSection.CRAFTING_IN_PERSISTENT, slots.subList(2, 11));
        } else if (isExact(guiScreen, "com.pahimar.ee3.client.gui.inventory.GuiPortableCrafting")) {
            result.put(ContainerSection.CRAFTING_OUT, slots.subList(0, 1));
            result.put(ContainerSection.CRAFTING_IN, slots.subList(1, 10));
        } else if (isExact(guiScreen, "micdoodle8.mods.galacticraft.core.client.gui.GCCoreGuiTankRefill")) {
            result.put(ContainerSection.CRAFTING_OUT, slots.subList(0, 1));
            result.put(ContainerSection.CRAFTING_IN, slots.subList(1, 5));
            result.put(ContainerSection.ARMOR, slots.subList(5, 9));
            result.put(ContainerSection.INVENTORY, slots.subList(9, 45));
            result.put(ContainerSection.INVENTORY_NOT_HOTBAR, slots.subList(9, 36));
            result.put(ContainerSection.INVENTORY_HOTBAR, slots.subList(36, 45));
        }

        return result;

    }

    private static boolean isExact(GuiScreen guiScreen, String className) {
        try {
            return guiScreen.getClass().getName().equals(className);
        } catch (Exception e) {
            return false;
        }
    }

    private static ContainerGUI getContainerGUIAnnotation(Class<? extends GuiScreen> clazz) {
        return clazz.getAnnotation(ContainerGUI.class);
    }

    private static InventoryGUI getInventoryGUIAnnotation(Class<? extends GuiScreen> clazz) {
        return clazz.getAnnotation(InventoryGUI.class);
    }

    private static boolean isAPIClass(Class<? extends GuiScreen> clazz) {
        return (getContainerGUIAnnotation(clazz) != null) || (getInventoryGUIAnnotation(clazz) != null);
    }

    @SuppressWarnings("unchecked")
    private static Method getAnnotatedMethod(Class clazz, Class[] annotations, int numParams, Class retClass) {
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            for (Class annotation : annotations) {
                if (m.getAnnotation(annotation) != null) {
                    if (m.getParameterTypes().length == numParams && retClass.isAssignableFrom(m.getReturnType())) {
                        return m;
                    }
                }
            }
        }
        return null;
    }
}
