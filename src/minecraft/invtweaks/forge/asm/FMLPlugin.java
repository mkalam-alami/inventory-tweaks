package invtweaks.forge.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.5.2")
@IFMLLoadingPlugin.TransformerExclusions({"invtweaks.forge.asm.ITAccessTransformer"})
public class FMLPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getLibraryRequestClass() {
        return new String[0];
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "invtweaks.forge.asm.ITAccessTransformer" };
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
    }
}
