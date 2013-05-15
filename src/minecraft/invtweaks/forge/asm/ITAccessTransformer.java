package invtweaks.forge.asm;

public class ITAccessTransformer extends RemappingAccessTransformer {
    public ITAccessTransformer() {
        readFile("invtweaks_at.cfg");
    }
}
