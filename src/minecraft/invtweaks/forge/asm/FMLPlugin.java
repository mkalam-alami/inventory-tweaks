package invtweaks.forge.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions({"invtweaks.forge.asm", "invtweaks.forge.asm.compatibility"})
@IFMLLoadingPlugin.MCVersion("") // We're using runtime debof integration, so no point in being specific about version
public class FMLPlugin implements IFMLLoadingPlugin {
    public static boolean runtimeDeobfEnabled = false;

    @Override
    @Deprecated
    public String[] getLibraryRequestClass() {
        return new String[0];
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{/*"invtweaks.forge.asm.ITAccessTransformer", */"invtweaks.forge.asm.ContainerTransformer"};
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
        runtimeDeobfEnabled = (Boolean)data.get("runtimeDeobfuscationEnabled");
    }
}
