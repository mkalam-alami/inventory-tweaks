package invtweaks.forge.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions({"invtweaks.forge.asm", "invtweaks.forge.asm.compatibility"})
@IFMLLoadingPlugin.MCVersion("") // We're using runtime debof integration, so no point in being specific about version
public class FMLPlugin implements IFMLLoadingPlugin {
    public static boolean runtimeDeobfEnabled = false;

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {"invtweaks.forge.asm.ContainerTransformer"};
    }

    @Override
    public String getAccessTransformerClass() {
        return "invtweaks.forge.asm.ITAccessTransformer";
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        runtimeDeobfEnabled = (Boolean) data.get("runtimeDeobfuscationEnabled");
    }
}
