import net.minecraft.client.Minecraft;

public abstract class BaseMod_InvTweaks {

	public abstract void load();

	public abstract String getVersion();

	public abstract void KeyboardEvent(afp keyBinding);

	public abstract boolean OnTickInGame(float clock, Minecraft minecraft);

	public abstract boolean OnTickInGUI(float clock, Minecraft minecraft, vl guiScreen);

	public abstract void OnItemPickup(yr entityplayer, aai stack);

}