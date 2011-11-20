import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

public abstract class ModLoader_InvTweaks {

    private static final Logger logger = Logger.getLogger("InvTweaks");

    private static final Map<BaseMod_InvTweaks, Boolean> inGameHooks = new HashMap<BaseMod_InvTweaks, Boolean>();
    
    private static final Map<BaseMod_InvTweaks, Boolean> inGUIHooks = new HashMap<BaseMod_InvTweaks, Boolean>();
    
    private static Minecraft instance;

    public static Minecraft getMinecraftInstance() {
        if (instance == null) {
            try {
                ThreadGroup group = Thread.currentThread().getThreadGroup();
                int count = group.activeCount();
                Thread[] threads = new Thread[count];
                group.enumerate(threads);
                for (int i = 0; i < threads.length; i++) {
                    System.out.println(threads[i].getName());
                }
                for (int i = 0; i < threads.length; i++)
                    if (threads[i].getName().equals("Minecraft main thread")) {
                        instance = (Minecraft) getPrivateValue(Thread.class, threads[i], "target");
                        break;
                    }
            } catch (SecurityException e) {
                logger.throwing("ModLoader", "getMinecraftInstance", e);
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                logger.throwing("ModLoader", "getMinecraftInstance", e);
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public static void RegisterKey(BaseMod_InvTweaks mod, aby keyBinding, boolean repeat) {
        // TODO
    }

    public static void SetInGameHook(BaseMod_InvTweaks mod, boolean enable, boolean useClock) {
        if (enable) inGameHooks.put(mod, Boolean.valueOf(useClock)); else
                 inGameHooks.remove(mod);
    }

    public static void SetInGUIHook(BaseMod_InvTweaks mod, boolean enable, boolean useClock) {

         if (enable) inGUIHooks.put(mod, Boolean.valueOf(useClock)); else
             inGUIHooks.remove(mod);
    }

    @SuppressWarnings("unchecked")
    public static <T, E> T getPrivateValue(Class<? super E> instanceclass, E instance, String field) throws IllegalArgumentException,
            SecurityException, NoSuchFieldException {
        try {
            Field f = instanceclass.getDeclaredField(field);
            f.setAccessible(true);
            return (T) f.get(instance);
        } catch (IllegalAccessException e) {
            logger.throwing("ModLoader", "getPrivateValue", e);
            ThrowException("An impossible error has occured!", e);
        }
        return null;
    }

    public static void ThrowException(String message, Throwable e) {
        Minecraft game = getMinecraftInstance();
        if (game != null)
            game.a(new oq(message, e)); /* MinecraftError (or sthg) */
        else
            throw new RuntimeException(e);
    }
    
}
