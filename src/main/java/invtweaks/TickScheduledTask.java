package invtweaks;

import java.util.Comparator;

public abstract class TickScheduledTask {
    private final long scheduledTickTime;

    public TickScheduledTask(long time) {
        scheduledTickTime = time;
    }

    public final long getScheduledTickTime() {
        return scheduledTickTime;
    }

    public abstract void run();
}
