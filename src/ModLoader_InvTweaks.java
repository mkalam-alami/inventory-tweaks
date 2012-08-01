import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

public abstract class ModLoader_InvTweaks {

    private static final Logger logger = Logger.getLogger("InvTweaks");

    private static Minecraft instance;

    private static final Map<ane, boolean[]> keyMap = new HashMap<ane, boolean[]>();

    private static long clock = 0L;

    private static InvTweaksObfuscation obf;

    /** Mod instanciation */
    private static BaseMod_InvTweaks mod;

    public static void init() throws Exception {
        if (instance == null) {
            instance = getMinecraftInstance();
            obf = new InvTweaksObfuscation(instance);
            instance.t = new EntityRendererProxy_InvTweaks(instance);
            
            // Hard-coded mod loading
            mod = new mod_InvTweaks();
            mod.load();
        }
    }

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

    public static void registerKey(BaseMod_InvTweaks mod, ane sortKeyBinding, boolean repeat) {
        keyMap.put(sortKeyBinding, new boolean[] { repeat, false });
        
        // Add binding to key settings
        ane[] registeredBindings = obf.getRegisteredBindings();
        boolean alreadyRegistered = false;
        for (ane registeredBinding : registeredBindings) {
            if (registeredBinding == sortKeyBinding) {
                alreadyRegistered = true;
            }
        }
        if (!alreadyRegistered) {
            ane[] newBindings = new ane[registeredBindings.length + 1];
            int i = 0;
            for (ane registeredBinding : registeredBindings) {
                newBindings[i] = registeredBinding;
                i++;
            }
            newBindings[i] = sortKeyBinding;
            obf.setRegisteredBindings(newBindings);
        }
        
    }

    public static void setInGameHook(BaseMod_InvTweaks mod, boolean enable, boolean useClock) {
        // Do nothing, InvTweaks is registered in the code
    }

    public static void setInGUIHook(BaseMod_InvTweaks mod, boolean enable, boolean useClock) {
        // Do nothing, InvTweaks is registered in the code
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
        if (game != null) {
            game.a(game.d(new a(message, e)));
        }
        else {
            throw new RuntimeException(e);
        }
    }

    public static void onTick(float tick, Minecraft game) throws Exception {

        long newclock = 0L;
        
        if (obf.getTheWorld() != null) {
            newclock = obf.getCurrentTime();
            if (clock != newclock) {
                mod.onTickInGame(0, game);
            }
        }
        if (obf.getCurrentScreen() != null && clock != newclock) {
            mod.onTickInGUI(0, instance, obf.getCurrentScreen());
        }

        clock = newclock;
    }

    public static void onItemPickup(of entityplayer, ri stack) {
        mod.onItemPickup(entityplayer, stack);
    }

}
