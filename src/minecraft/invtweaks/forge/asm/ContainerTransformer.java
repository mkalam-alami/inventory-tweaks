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
        standardClasses.put("net.minecraft.inventory.ContainerDispenser", new ContainerInfo(false, false, true));
        standardClasses.put("net.minecraft.inventory.ContainerChest", new ContainerInfo(false, false, true));
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        FMLRelaunchLog.info(String.format("%s = %s", name, transformedName));

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

    private void transformContainer(ClassNode clazz, ContainerInfo info) {
        generateBooleanMethodConst(clazz, "invtweaks$standardInventory", info.standardInventory);
        generateBooleanMethodConst(clazz, "invtweaks$validInventory", info.validInventory);
        generateBooleanMethodConst(clazz, "invtweaks$validChest", info.validChest);
    }

    private void generateBooleanMethodConst(ClassNode clazz, String name, boolean retval) {
        MethodNode method = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC|Opcodes.ACC_SYNTHETIC, name, "()Z", null, null);
        InsnList code = method.instructions;

        code.add(new InsnNode(retval?Opcodes.ICONST_1:Opcodes.ICONST_0));
        code.add(new InsnNode(Opcodes.IRETURN));

        clazz.methods.add(method);
    }


    class ContainerInfo {
        boolean standardInventory = false;
        boolean validInventory = false;
        boolean validChest = false;

        ContainerInfo() { }

        ContainerInfo(boolean standard, boolean validInv, boolean validCh) {
            standardInventory = standard;
            validInventory = validInv;
            validChest = validCh;
        }
    }
}
