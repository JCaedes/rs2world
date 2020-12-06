package be.caedes.rs2;

public class Utility {

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
