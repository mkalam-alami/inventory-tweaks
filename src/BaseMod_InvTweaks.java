import net.minecraft.client.Minecraft;

public abstract class BaseMod_InvTweaks {

    public abstract void load();

    public abstract String getVersion();

    public abstract boolean onTickInGame(float clock, Minecraft minecraft);

    public abstract boolean onTickInGUI(float clock, Minecraft minecraft, apm guiScreen);

    public abstract void onItemPickup(of entityplayer, ri stack);

}