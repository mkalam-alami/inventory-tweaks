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

    abstract void run();

    public static final class TaskComparator implements Comparator<TickScheduledTask> {
        @Override
        public int compare(TickScheduledTask o1, TickScheduledTask o2) {
            // TODO: Kinda inefficient, but the better version is J1.7+
            // Might be worth considering that move as 1.6 is quite old now
            // and many mods require it already.
            return Long.valueOf(o1.scheduledTickTime).compareTo(Long.valueOf(o2.scheduledTickTime));
        }
    }
}
