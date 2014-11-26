package invtweaks.forge;

import invtweaks.InvTweaks;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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
