package invtweaks.forge.asm;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;


public class ContainerTransformer implements IClassTransformer {
    private static Map<String, ContainerInfo> standardClasses = new HashMap<String, ContainerInfo>();

    public ContainerTransformer() {
        // Default implementations (will need special-cased later because of how isValidInventory works)
        standardClasses.put("net.minecraft.inventory.Container", new ContainerInfo());

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
