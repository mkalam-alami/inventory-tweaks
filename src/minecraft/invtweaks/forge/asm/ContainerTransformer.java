package invtweaks.forge.asm;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;


public class ContainerTransformer implements IClassTransformer {
    private static Map<String, ContainerInfo> standardClasses = new HashMap<String, ContainerInfo>();

    public ContainerTransformer() {
        // Standard non-chest type
        standardClasses.put("net.minecraft.inventory.ContainerPlayer", new ContainerInfo(true, true, false));
        standardClasses.put("net.minecraft.inventory.ContainerMerchant", new ContainerInfo(true, true, false));
        standardClasses.put("net.minecraft.inventory.ContainerRepair", new ContainerInfo(true, true, false));
        standardClasses.put("net.minecraft.inventory.ContainerHopper", new ContainerInfo(true, true, false));
        standardClasses.put("net.minecraft.inventory.ContainerBeacon", new ContainerInfo(true, true, false));
        standardClasses.put("net.minecraft.inventory.ContainerBrewingStand", new ContainerInfo(true, true, false));
        standardClasses.put("net.minecraft.inventory.ContainerWorkbench", new ContainerInfo(true, true, false));
        standardClasses.put("net.minecraft.inventory.ContainerEnchantment", new ContainerInfo(true, true, false));
        standardClasses.put("net.minecraft.inventory.ContainerFurnace", new ContainerInfo(true, true, false));

        // Chest-type
        standardClasses.put("net.minecraft.inventory.ContainerDispenser", new ContainerInfo(false, false, true, (short)3));
        standardClasses.put("net.minecraft.inventory.ContainerChest", new ContainerInfo(false, false, true));
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        FMLRelaunchLog.info(String.format("%s = %s", name, transformedName));

        if("net.minecraft.inventory.Container".equals(transformedName)) {
            ClassReader cr = new ClassReader(bytes);
            ClassNode cn = new ClassNode(Opcodes.ASM4);
            cr.accept(cn, 0);

            transformBaseContainer(cn);

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            return cw.toByteArray();
        }

        // Transform classes with explicitly specified information
        ContainerInfo info = standardClasses.get(transformedName);
        if(info != null) {
            ClassReader cr = new ClassReader(bytes);
            ClassNode cn = new ClassNode(Opcodes.ASM4);
            cr.accept(cn, 0);

            transformContainer(cn, info);

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            return cw.toByteArray();
        }

        if("invtweaks.InvTweaksObfuscation".equals(transformedName)) {
            ClassReader cr = new ClassReader(bytes);
            ClassNode cn = new ClassNode(Opcodes.ASM4);
            cr.accept(cn, 0);

            Type thistype = Type.getObjectType(cn.name);
            for(MethodNode method : cn.methods) {
                if("isValidChest".equals(method.name))  {
                    replaceForwardingMethod(method, "invtweaks$validChest", thistype);
                } else if("isValidInventory".equals(method.name)) {
                    replaceForwardingMethod(method, "invtweaks$validInventory", thistype);
                } else if("isStandardInventory".equals(method.name)) {
                    replaceForwardingMethod(method, "invtweaks$standardInventory", thistype);
                } else if("getSpecialChestRowSize".equals(method.name)) {
                    replaceForwardingMethod(method, "invtweaks$rowSize", thistype);
                }
            }

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            return cw.toByteArray();
        }

        return bytes;
    }

    /**
     * Alter class to contain information contained by ContainerInfo
     *
     * @param clazz Class to alter
     * @param info Information used to alter class
     */
    private void transformContainer(ClassNode clazz, ContainerInfo info) {
        generateBooleanMethodConst(clazz, "invtweaks$standardInventory", info.standardInventory);
        generateBooleanMethodConst(clazz, "invtweaks$validInventory", info.validInventory);
        generateBooleanMethodConst(clazz, "invtweaks$validChest", info.validChest);
        generateIntegerMethodConst(clazz, "invtweaks$rowSize", info.rowSize);
    }


    /**
     * Alter class to contain default implementations of added methods.
     *
     * @param clazz Class to alter
     */
    private void transformBaseContainer(ClassNode clazz) {
        generateBooleanMethodConst(clazz, "invtweaks$standardInventory", false);
        generateDefaultInventoryCheck(clazz);
        generateBooleanMethodConst(clazz, "invtweaks$validChest", false);
        generateIntegerMethodConst(clazz, "invtweaks$rowSize", (short) 9);
    }

    /**
     * Generate a new method "boolean invtweaks$validInventory()", returning true
     * if the size of the container is large enough to hold the player inventory.
     *
     * @param clazz Class to add method to
     */
    private void generateDefaultInventoryCheck(ClassNode clazz) {
        MethodNode method = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC|Opcodes.ACC_SYNTHETIC, "invtweaks$validInventory", "()Z", null, null);
        InsnList code = method.instructions;

        LabelNode start = new LabelNode();
        code.add(start);

        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new FieldInsnNode(Opcodes.GETFIELD, clazz.name, "field_75151_b", "Ljava/util/List;"));
        code.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "size", "()I"));
        code.add(new IntInsnNode(Opcodes.BIPUSH, 36)); // TODO: Load Static InvTweaksConst.INVENTORY_SIZE

        LabelNode l1 = new LabelNode();
        code.add(new JumpInsnNode(Opcodes.IF_ICMPLE, l1));
        code.add(new InsnNode(Opcodes.ICONST_1));

        LabelNode l2 = new LabelNode();
        code.add(new JumpInsnNode(Opcodes.GOTO, l2));

        code.add(l1);
        code.add(new InsnNode(Opcodes.ICONST_0));

        code.add(l2);
        code.add(new InsnNode(Opcodes.IRETURN));

        LabelNode end = new LabelNode();
        code.add(end);

        method.localVariables.add(new LocalVariableNode("this", Type.getObjectType(clazz.name).getDescriptor(), null, start, end, 0));

        clazz.methods.add(method);
    }

    /**
     * Generate a new method "boolean name()", returning a constant value
     *
     * @param clazz Class to add method to
     * @param name Name of method
     * @param retval Return value of method
     */
    private void generateBooleanMethodConst(ClassNode clazz, String name, boolean retval) {
        MethodNode method = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC|Opcodes.ACC_SYNTHETIC, name, "()Z", null, null);
        InsnList code = method.instructions;

        code.add(new InsnNode(retval?Opcodes.ICONST_1:Opcodes.ICONST_0));
        code.add(new InsnNode(Opcodes.IRETURN));

        clazz.methods.add(method);
    }

    /**
     * Generate a new method "int name()", returning a constant value
     *
     * @param clazz Class to add method to
     * @param name Name of method
     * @param retval Return value of method
     */
    private void generateIntegerMethodConst(ClassNode clazz, String name, short retval) {
        MethodNode method = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC|Opcodes.ACC_SYNTHETIC, name, "()I", null, null);
        InsnList code = method.instructions;

        // Probably doesn't make a huge difference, but use BIPUSH if the value is small enough.
        if(retval >= Byte.MIN_VALUE && retval <= Byte.MAX_VALUE) {
            code.add(new IntInsnNode(Opcodes.BIPUSH, retval));
        } else {
            code.add(new IntInsnNode(Opcodes.SIPUSH, retval));
        }
        code.add(new InsnNode(Opcodes.IRETURN));

        clazz.methods.add(method);
    }

    /**
     * Generate a forwarding method of the form "T name(S object) { return S.forward(); }
     *
     * @param clazz Class to generate new method on
     * @param name Name of method to generate
     * @param forwardname Name of method to call
     * @param rettype Return type of method
     * @param argtype Type of object to call method on
     */
    private void generateForwardingMethod(ClassNode clazz, String name, String forwardname, Type rettype, Type argtype) {
        MethodNode method = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC|Opcodes.ACC_SYNTHETIC, name, "()" + rettype.getDescriptor(), null, null);

        populateForwardingMethod(method, forwardname, rettype, argtype, Type.getObjectType(clazz.name));

        clazz.methods.add(method);
    }

    /**
     * Replace a method's code with a forward to an method on its first argument
     *
     * @param method Method to replace code of
     * @param forwardname Name of method to forward to
     * @param thistype Type of object method is being replaced on
     */
    private void replaceForwardingMethod(MethodNode method, String forwardname, Type thistype) {
        Type methodType = Type.getMethodType(method.desc);

        method.instructions.clear();

        populateForwardingMethod(method, forwardname, methodType.getReturnType(), methodType.getArgumentTypes()[0], thistype);
    }

    /**
     * Populate a forwarding method of the form "T name(S object) { return S.forward(); }
     *
     * @param method Method to generate code for
     * @param forwardname Name of method to call
     * @param rettype Return type of method
     * @param argtype Type of object to call method on
     * @param thistype Type of object method is being generated on
     */
    private void populateForwardingMethod(MethodNode method, String forwardname, Type rettype, Type argtype, Type thistype) {
        InsnList code = method.instructions;

        LabelNode start = new LabelNode();
        code.add(start);

        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, argtype.getInternalName(), forwardname, "()" + rettype.getDescriptor()));
        code.add(new InsnNode(rettype.getOpcode(Opcodes.IRETURN)));

        LabelNode end = new LabelNode();
        code.add(end);

        method.localVariables.add(new LocalVariableNode("this", thistype.getDescriptor(), null, start, end, 0));
        method.localVariables.add(new LocalVariableNode("arg", argtype.getDescriptor(), null, start, end, 1));
    }

    private int test(ContainerInfo a) {
        return a.hashCode();
    }

    class ContainerInfo {
        boolean standardInventory = false;
        boolean validInventory = false;
        boolean validChest = false;
        short rowSize = 9;

        ContainerInfo() { }

        ContainerInfo(boolean standard, boolean validInv, boolean validCh) {
            standardInventory = standard;
            validInventory = validInv;
            validChest = validCh;
        }

        ContainerInfo(boolean standard, boolean validInv, boolean validCh, short rowS) {
            standardInventory = standard;
            validInventory = validInv;
            validChest = validCh;
            rowSize = rowS;
        }
    }
}
