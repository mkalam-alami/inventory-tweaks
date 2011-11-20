import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;

public abstract class ModLoader_InvTweaks {

    private static final Logger logger = Logger.getLogger("InvTweaks");

    private static Minecraft instance;

    private static final Map<aby, boolean[]> keyMap = new HashMap<aby, boolean[]>();

    private static long clock = 0L;

    private static InvTweaksObfuscation obf;

    /** Mod instanciation */
    private static final BaseMod_InvTweaks mod = new mod_InvTweaks();

    public static void init() {
        instance = getMinecraftInstance();
        obf = new InvTweaksObfuscation(instance);
        instance.u = new EntityRendererProxy_InvTweaks(instance);
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

    public static void RegisterKey(BaseMod_InvTweaks mod, aby keyBinding, boolean repeat) {
        keyMap.put(keyBinding, new boolean[] { repeat, false });
    }

    public static void SetInGameHook(BaseMod_InvTweaks mod, boolean enable, boolean useClock) {
        // Do nothing, InvTweaks is registered in the code
    }

    public static void SetInGUIHook(BaseMod_InvTweaks mod, boolean enable, boolean useClock) {
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
        if (game != null)
            game.a(new oq(message, e)); /* MinecraftError (or sthg) */
        else
            throw new RuntimeException(e);
    }

    public static void OnTick(float tick, Minecraft game) {

        long newclock = 0L;

        if (obf.getTheWorld() != null) {
            newclock = obf.getTheWorld().t(); // getCurrentTime()
            if (clock != newclock) {
                mod.OnTickInGame(game);
            }
        }
        if (obf.getCurrentScreen() != null && clock != newclock) {
            mod.OnTickInGUI(instance, obf.getCurrentScreen());
        }
        if (clock != newclock) {
            for (aby keyBinding : keyMap.keySet()) {
                boolean state = Keyboard.isKeyDown(keyBinding.d);
                boolean[] keyInfo = keyMap.get(keyBinding);
                boolean oldState = keyInfo[1];
                keyInfo[1] = state;
                if ((state) && (!oldState || keyInfo[0])) {
                    mod.KeyboardEvent(keyBinding);
                }
            }
        }

        clock = newclock;
    }

    public static void OnItemPickup(vi entityplayer, dk stack) {
        mod.OnItemPickup(entityplayer, stack);
    }

}
