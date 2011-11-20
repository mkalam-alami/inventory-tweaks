import net.minecraft.client.Minecraft;


public interface BaseMod_InvTweaks {

    String Version();

    void KeyboardEvent(aby keyBinding);
    
    boolean OnTickInGUI(Minecraft game, xe guiScreen);
    
    boolean OnTickInGame(Minecraft minecraft);
    
    void OnItemPickup(vi entityplayer, dk stack);
    
}
