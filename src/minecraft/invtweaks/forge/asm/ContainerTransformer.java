package invtweaks.forge.asm;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.Map;


public class ContainerTransformer implements IClassTransformer {
    public static final String VALID_INVENTORY_METHOD = "invtweaks$validInventory";
    public static final String VALID_CHEST_METHOD = "invtweaks$validChest";
    public static final String LARGE_CHEST_METHOD = "invtweaks$largeChest";
    public static final String STANDARD_INVENTORY_METHOD = "invtweaks$standardInventory";
    public static final String ROW_SIZE_METHOD = "invtweaks$rowSize";
    public static final String SLOT_MAP_METHOD = "invtweaks$slotMap";
    public static final String CONTAINER_CLASS_INTERNAL = "net/minecraft/inventory/Container";
    public static final String SLOT_MAPS_VANILLA_CLASS = "invtweaks/containers/VanillaSlotMaps";
    public static final String SLOT_MAPS_MODCOMPAT_CLASS = "invtweaks/containers/CompatibilitySlotMaps";
    public static final String ANNOTATION_CHEST_CONTAINER = "Linvtweaks/api/container/ChestContainer;";
    public static final String ANNOTATION_CHEST_CONTAINER_ROW_CALLBACK =
            "Linvtweaks/api/container/ChestContainer$RowSizeCallback;";
    public static final String ANNOTATION_INVENTORY_CONTAINER = "Linvtweaks/api/container/InventoryContainer;";
    public static final String ANNOTATION_CONTAINER_SECTION_CALLBACK =
            "Linvtweaks/api/container/ContainerSectionCallback;";

    private static Map<String, ContainerInfo> standardClasses = new HashMap<String, ContainerInfo>();
    private static Map<String, ContainerInfo> compatibilityClasses = new HashMap<String, ContainerInfo>();
    private String containerClassName;

    public ContainerTransformer() {
    }

    // This needs to have access to the FML remapper so it needs to run when we know it's been set up correctly.
    private void lateInit() {
        // TODO: ContainerCreative handling
        // Standard non-chest type
        standardClasses.put("net.minecraft.inventory.ContainerPlayer",
                            new ContainerInfo(true, true, false, getVanillaSlotMapInfo("containerPlayerSlots")));
        standardClasses.put("net.minecraft.inventory.ContainerMerchant", new ContainerInfo(true, true, false));
        standardClasses.put("net.minecraft.inventory.ContainerRepair",
                            new ContainerInfo(true, true, false, getVanillaSlotMapInfo("containerPlayerSlots")));
        standardClasses.put("net.minecraft.inventory.ContainerHopper", new ContainerInfo(true, true, false));
        standardClasses.put("net.minecraft.inventory.ContainerBeacon", new ContainerInfo(true, true, false));
        standardClasses.put("net.minecraft.inventory.ContainerBrewingStand",
                            new ContainerInfo(true, true, false, getVanillaSlotMapInfo("containerBrewingSlots")));
        standardClasses.put("net.minecraft.inventory.ContainerWorkbench",
                            new ContainerInfo(true, true, false, getVanillaSlotMapInfo("containerWorkbenchSlots")));
        standardClasses.put("net.minecraft.inventory.ContainerEnchantment",
                            new ContainerInfo(true, true, false, getVanillaSlotMapInfo("containerEnchantmentSlots")));
        standardClasses.put("net.minecraft.inventory.ContainerFurnace",
                            new ContainerInfo(true, true, false, getVanillaSlotMapInfo("containerFurnaceSlots")));

        // Chest-type
        standardClasses.put("net.minecraft.inventory.ContainerDispenser",
                            new ContainerInfo(false, false, true, (short) 3,
                                              getVanillaSlotMapInfo("containerChestDispenserSlots")));
        standardClasses.put("net.minecraft.inventory.ContainerChest", new ContainerInfo(false, false, true,
                                                                                        getVanillaSlotMapInfo(
                                                                                                "containerChestDispenserSlots")));

        // Mod compatibility
        // Iron Chests
        ContainerInfo ironChestsInfo = new ContainerInfo(false, false, true, true);
        // TODO Iron Chest row size method. (Needs to be injected because it exists on the GUI, although the container has the info needed)
        compatibilityClasses.put("cpw.mods.ironchest.ContainerIronChestBase", ironChestsInfo);

        // Equivalent Exchange 3
        compatibilityClasses.put("com.pahimar.ee3.inventory.ContainerAlchemicalBag",
                                 new ContainerInfo(false, false, true, true, (short)13));
        compatibilityClasses.put("com.pahimar.ee3.inventory.ContainerAlchemicalChest",
                                 new ContainerInfo(false, false, true, true, (short)13));
        compatibilityClasses.put("com.pahimar.ee3.inventory.ContainerPortableCrafting",
                                 new ContainerInfo(true, true, false,
                                                   getCompatiblitySlotMapInfo("ee3PortableCraftingSlots")));

        // Better Storage
        ContainerInfo betterStorageInfo = new ContainerInfo(false, false, true);
        // TODO Better Storage row size method. (Generate an accessor method for field 'columns')
        compatibilityClasses.put("net.mcft.copy.betterstorage.container.ContainerBetterStorage", betterStorageInfo);

        // Ender Storage
        // TODO blahblah see above. A bit less important because it's a config setting and 2 of 3 options give rowsize 9.
        compatibilityClasses.put("codechicken.enderstorage.storage.item.ContainerEnderItemStorage",
                                 new ContainerInfo(false, false, true));

        // Galacticraft
        compatibilityClasses.put("micdoodle8.mods.galacticraft.core.inventory.GCCoreContainerPlayer",
                                 new ContainerInfo(true, true, false,
                                                   getCompatiblitySlotMapInfo("galacticraftPlayerSlots")));
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if(containerClassName == null) {
            if(FMLPlugin.runtimeDeobfEnabled) {
                containerClassName = FMLDeobfuscatingRemapper.INSTANCE.unmap(CONTAINER_CLASS_INTERNAL);
            } else {
                containerClassName = CONTAINER_CLASS_INTERNAL;
            }
            lateInit();
        }

        ClassReader cr = new ClassReader(bytes);
        ClassNode cn = new ClassNode(Opcodes.ASM4);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        cr.accept(cn, 0);

        if("net.minecraft.inventory.Container".equals(transformedName)) {
            FMLRelaunchLog.info("InvTweaks: %s", transformedName);

            transformBaseContainer(cn);

            cn.accept(cw);
            return cw.toByteArray();
        }

        if("net.minecraft.client.gui.inventory.ContainerCreative".equals(transformedName)) {
            FMLRelaunchLog.info("InvTweaks: %s", transformedName);

            transformCreativeContainer(cn);

            cn.accept(cw);
            return cw.toByteArray();
        }

        // Transform classes with explicitly specified information
        ContainerInfo info = standardClasses.get(transformedName);
        if(info != null) {
            FMLRelaunchLog.info("InvTweaks: %s", transformedName);

            transformContainer(cn, info);

            cn.accept(cw);
            return cw.toByteArray();
        }

        if("invtweaks.InvTweaksObfuscation".equals(transformedName)) {
            FMLRelaunchLog.info("InvTweaks: %s", transformedName);

            Type containertype =
                    Type.getObjectType(containerClassName);
            for(MethodNode method : cn.methods) {
                if("isValidChest".equals(method.name)) {
                    ASMHelper.replaceSelfForwardingMethod(method, VALID_CHEST_METHOD, containertype);
                } else if("isValidInventory".equals(method.name)) {
                    ASMHelper.replaceSelfForwardingMethod(method, VALID_INVENTORY_METHOD, containertype);
                } else if("isStandardInventory".equals(method.name)) {
                    ASMHelper.replaceSelfForwardingMethod(method, STANDARD_INVENTORY_METHOD, containertype);
                } else if("getSpecialChestRowSize".equals(method.name)) {
                    ASMHelper.replaceSelfForwardingMethod(method, ROW_SIZE_METHOD, containertype);
                } else if("getContainerSlotMap".equals(method.name)) {
                    ASMHelper.replaceSelfForwardingMethod(method, SLOT_MAP_METHOD, containertype);
                } else if("isLargeChest".equals(method.name)) {
                    ASMHelper.replaceSelfForwardingMethod(method, LARGE_CHEST_METHOD, containertype);
                }
            }

            cn.accept(cw);
            return cw.toByteArray();
        }

        if(cn.visibleAnnotations != null) {
            for(AnnotationNode annotation : cn.visibleAnnotations) {
                if(annotation != null) {
                    ContainerInfo apiInfo = null;

                    if(ANNOTATION_CHEST_CONTAINER.equals(annotation.desc)) {
                        apiInfo = new ContainerInfo(false, false, true, (Boolean)annotation.values.get(1),
                                                    (short) ((Integer) annotation.values.get(0)).intValue());

                        MethodNode method = findAnnotatedMethod(cn, ANNOTATION_CHEST_CONTAINER_ROW_CALLBACK);

                        if(method != null) {
                            apiInfo.rowSizeMethod =
                                    new MethodInfo(Type.getMethodType(method.desc), Type.getObjectType(cn.name),
                                                   method.name);
                        }
                    } else if(ANNOTATION_INVENTORY_CONTAINER.equals(annotation.desc)) {
                        apiInfo = new ContainerInfo((Boolean) annotation.values.get(0), true, false);
                    }

                    if(apiInfo != null) {
                        // Search methods to see if any have the ContainerSectionCallback attribute.
                        MethodNode method = findAnnotatedMethod(cn, ANNOTATION_CONTAINER_SECTION_CALLBACK);

                        if(method != null) {
                            apiInfo.slotMapMethod =
                                    new MethodInfo(Type.getMethodType(method.desc), Type.getObjectType(cn.name),
                                                   method.name);
                        }

                        transformContainer(cn, apiInfo);
                    }
                }
            }
        }

        info = compatibilityClasses.get(transformedName);
        if(info != null) {
            FMLRelaunchLog.info("InvTweaks: %s", transformedName);

            transformContainer(cn, info);

            cn.accept(cw);
            return cw.toByteArray();
        }

        return bytes;
    }

    private MethodNode findAnnotatedMethod(ClassNode cn, String annotationDesc) {
        for(MethodNode method : cn.methods) {
            if(method.visibleAnnotations != null) {
                for(AnnotationNode methodAnnotation : method.visibleAnnotations) {
                    if(annotationDesc.equals(methodAnnotation.desc)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Alter class to contain information contained by ContainerInfo
     *
     * @param clazz Class to alter
     * @param info  Information used to alter class
     */
    public static void transformContainer(ClassNode clazz, ContainerInfo info) {
        ASMHelper.generateBooleanMethodConst(clazz, STANDARD_INVENTORY_METHOD, info.standardInventory);
        ASMHelper.generateBooleanMethodConst(clazz, VALID_INVENTORY_METHOD, info.validInventory);
        ASMHelper.generateBooleanMethodConst(clazz, VALID_CHEST_METHOD, info.validChest);
        ASMHelper.generateBooleanMethodConst(clazz, LARGE_CHEST_METHOD, info.largeChest);

        if(info.rowSizeMethod != null) {
            if(info.rowSizeMethod.isStatic) {
                ASMHelper.generateForwardingToStaticMethod(clazz, ROW_SIZE_METHOD, info.rowSizeMethod.methodName,
                                                           info.rowSizeMethod.methodType.getReturnType(),
                                                           info.rowSizeMethod.methodClass,
                                                           info.rowSizeMethod.methodType.getArgumentTypes()[0]);
            } else {
                ASMHelper.generateSelfForwardingMethod(clazz, ROW_SIZE_METHOD, info.rowSizeMethod.methodName,
                                                       info.rowSizeMethod.methodType);
            }
        } else {
            ASMHelper.generateIntegerMethodConst(clazz, ROW_SIZE_METHOD, info.rowSize);
        }

        if(info.slotMapMethod.isStatic) {
            ASMHelper.generateForwardingToStaticMethod(clazz, SLOT_MAP_METHOD, info.slotMapMethod.methodName,
                                                       info.slotMapMethod.methodType.getReturnType(),
                                                       info.slotMapMethod.methodClass,
                                                       info.slotMapMethod.methodType.getArgumentTypes()[0]);
        } else {
            ASMHelper.generateSelfForwardingMethod(clazz, SLOT_MAP_METHOD, info.slotMapMethod.methodName,
                                                   info.slotMapMethod.methodType);
        }
    }

    /**
     * Alter class to contain default implementations of added methods.
     *
     * @param clazz Class to alter
     */
    public static void transformBaseContainer(ClassNode clazz) {
        ASMHelper.generateBooleanMethodConst(clazz, STANDARD_INVENTORY_METHOD, false);
        ASMHelper.generateDefaultInventoryCheck(clazz);
        ASMHelper.generateBooleanMethodConst(clazz, VALID_CHEST_METHOD, false);
        ASMHelper.generateBooleanMethodConst(clazz, LARGE_CHEST_METHOD, false);
        ASMHelper.generateIntegerMethodConst(clazz, ROW_SIZE_METHOD, (short) 9);
        ASMHelper.generateForwardingToStaticMethod(clazz, SLOT_MAP_METHOD, "unknownContainerSlots",
                                                   Type.getObjectType("java/util/Map"),
                                                   Type.getObjectType(SLOT_MAPS_VANILLA_CLASS));
    }

    public static void transformCreativeContainer(ClassNode clazz) {
        ASMHelper.generateForwardingToStaticMethod(clazz, STANDARD_INVENTORY_METHOD, "containerCreativeIsInventory",
                                                   Type.BOOLEAN_TYPE, Type.getObjectType(SLOT_MAPS_VANILLA_CLASS));
        ASMHelper.generateForwardingToStaticMethod(clazz, VALID_INVENTORY_METHOD, "containerCreativeIsInventory",
                                                   Type.BOOLEAN_TYPE, Type.getObjectType(SLOT_MAPS_VANILLA_CLASS));
        ASMHelper.generateBooleanMethodConst(clazz, VALID_CHEST_METHOD, false);
        ASMHelper.generateBooleanMethodConst(clazz, LARGE_CHEST_METHOD, false);
        ASMHelper.generateIntegerMethodConst(clazz, ROW_SIZE_METHOD, (short) 9);
        ASMHelper.generateForwardingToStaticMethod(clazz, SLOT_MAP_METHOD, "containerCreativeSlots",
                                                   Type.getObjectType("java/util/Map"),
                                                   Type.getObjectType(SLOT_MAPS_VANILLA_CLASS));
    }


    private MethodInfo getCompatiblitySlotMapInfo(String name) {
        return getSlotMapInfo(Type.getObjectType(SLOT_MAPS_MODCOMPAT_CLASS), name, true);
    }

    private MethodInfo getVanillaSlotMapInfo(String name) {
        return getSlotMapInfo(Type.getObjectType(SLOT_MAPS_VANILLA_CLASS), name, true);
    }

    private MethodInfo getSlotMapInfo(Type mClass, String name, boolean isStatic) {
        return new MethodInfo(Type.getMethodType(
                Type.getObjectType("java/util/Map"),
                Type.getObjectType(containerClassName)),
                              mClass, name, isStatic);
    }

    class MethodInfo {
        Type methodType;
        Type methodClass;
        String methodName;
        boolean isStatic = false;

        MethodInfo(Type mType, Type mClass, String name) {
            methodType = mType;
            methodClass = mClass;
            methodName = name;
        }

        MethodInfo(Type mType, Type mClass, String name, boolean stat) {
            methodType = mType;
            methodClass = mClass;
            methodName = name;
            isStatic = stat;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    class ContainerInfo {
        boolean standardInventory = false;
        boolean validInventory = false;
        boolean validChest = false;
        boolean largeChest = false;
        short rowSize = 9;
        MethodInfo slotMapMethod = getVanillaSlotMapInfo("unknownContainerSlots");
        MethodInfo rowSizeMethod = null;

        ContainerInfo() {
        }

        ContainerInfo(boolean standard, boolean validInv, boolean validCh) {
            standardInventory = standard;
            validInventory = validInv;
            validChest = validCh;
        }

        ContainerInfo(boolean standard, boolean validInv, boolean validCh, boolean largeCh) {
            standardInventory = standard;
            validInventory = validInv;
            validChest = validCh;
            largeChest = largeCh;
        }

        ContainerInfo(boolean standard, boolean validInv, boolean validCh, MethodInfo slotMap) {
            standardInventory = standard;
            validInventory = validInv;
            validChest = validCh;
            slotMapMethod = slotMap;
        }

        ContainerInfo(boolean standard, boolean validInv, boolean validCh, short rowS) {
            standardInventory = standard;
            validInventory = validInv;
            validChest = validCh;
            rowSize = rowS;
        }

        ContainerInfo(boolean standard, boolean validInv, boolean validCh, boolean largeCh, short rowS) {
            standardInventory = standard;
            validInventory = validInv;
            validChest = validCh;
            largeChest = largeCh;
            rowSize = rowS;
        }

        ContainerInfo(boolean standard, boolean validInv, boolean validCh, short rowS, MethodInfo slotMap) {
            standardInventory = standard;
            validInventory = validInv;
            validChest = validCh;
            rowSize = rowS;
            slotMapMethod = slotMap;
        }
    }
}
