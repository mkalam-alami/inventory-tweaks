package invtweaks.forge;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ServerProxy extends CommonProxy {
    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent tick) {
        if(tick.phase == TickEvent.Phase.START) {
            runScheduledTasks(getCurrentTick());
        }
    }
}
