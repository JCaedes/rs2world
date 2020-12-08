package be.caedes.rs2;

import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utility {

    public static final ExecutorService io = Executors.newSingleThreadExecutor();

    public static final SecureRandom rng = new SecureRandom();

    public static long elapsedMillis(long moment) {
        return System.currentTimeMillis() - moment;
    }

    public static boolean elapsedMillis(long moment, long period) {
        return elapsedMillis(moment) >= period;
    }

    public static int elapsedTicks(int moment) {
        return RS2World.getCurrentTick() - moment;
    }

    public static boolean elapsedTicks(int moment, int period) {
        return elapsedTicks(moment) >= period;
    }

}
