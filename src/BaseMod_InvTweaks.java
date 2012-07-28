import net.minecraft.client.Minecraft;

public abstract class BaseMod_InvTweaks {

    public abstract void load();

    public abstract String getVersion();

    public abstract boolean onTickInGame(float clock, Minecraft minecraft);

    public abstract boolean onTickInGUI(float clock, Minecraft minecraft, anm guiScreen);

    public abstract void onItemPickup(np entityplayer, qs stack);

}