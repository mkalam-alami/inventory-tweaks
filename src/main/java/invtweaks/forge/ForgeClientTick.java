package invtweaks.forge;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import invtweaks.InvTweaks;
import net.minecraft.client.Minecraft;

public class ForgeClientTick {
    private InvTweaks instance;

    public ForgeClientTick(InvTweaks inst) {
        instance = inst;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick) {
        if(tick.phase == TickEvent.Phase.START) {
            Minecraft mc = FMLClientHandler.instance().getClient();
            if(mc.theWorld != null) {
                if(mc.currentScreen != null) {
                    instance.onTickInGUI(mc.currentScreen);
                } else {
                    instance.onTickInGame();
                }
            }
        }
    }
}
