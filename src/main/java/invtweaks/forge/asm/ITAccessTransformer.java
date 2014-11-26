package invtweaks.forge.asm;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public class ITAccessTransformer extends AccessTransformer {
    public ITAccessTransformer() throws IOException {
        super("invtweaks_at.cfg");
    }
}
