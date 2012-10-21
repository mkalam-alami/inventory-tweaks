import net.minecraft.client.Minecraft;

public abstract class BaseMod_InvTweaks {

    public abstract void load();

    public abstract String getName();
    
    public abstract String getVersion();

    public abstract boolean onTickInGame(float clock, Minecraft minecraft);

    public abstract void onItemPickup(qf entityplayer, tu stack);

    
}