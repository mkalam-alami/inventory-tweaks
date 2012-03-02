import net.minecraft.client.Minecraft;

public class EntityRendererProxy_InvTweaks extends lo {
    private Minecraft game;

    public EntityRendererProxy_InvTweaks(Minecraft minecraft) {
        super(minecraft);
        this.game = minecraft;
    }

    public void b(float tick) {
        super.b(tick);
        ModLoader_InvTweaks.OnTick(tick, this.game);
    }
}
