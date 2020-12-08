package be.caedes.rs2;

import be.caedes.rs2.net.Server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class RS2World {

    private static final int PORT = 43594;
    private static final int CAPACITY = 200;

    private static int currentTick = 0;
    private static Server server;
    private static final HashMap<Integer, LinkedList<Event>> eventMap = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("INFO: Attempting to open rs2 server on port: "+PORT);
        server = new Server(PORT, CAPACITY);
        Thread serverThread = new Thread(server);
        serverThread.start();

        boolean running = true;
        while (running) {
            currentTick++;
            long start = System.currentTimeMillis();

            //start server logic here
            triggerScheduledEvents();

            long sleepDuration = 600 - (System.currentTimeMillis() - start);
            try {
                if (sleepDuration > 0) {
                    Thread.sleep(sleepDuration);
                } else {
                    System.out.println("WARNING: Server under severe load: current lag: "+sleepDuration+" ms.");
                }
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    public static int getCurrentTick() {
        return currentTick;
    }

    public static void scheduleEvent(Event event) {
        scheduleEvent(event, 1);
    }

    public static void scheduleEvent(Event event, int delay) {
        if (delay < 0) return;
        int targetTick = currentTick + delay;
        synchronized (eventMap) {
            LinkedList<Event> eventList = eventMap.get(targetTick);
            if (eventList == null) {
                eventList = new LinkedList<>();
                eventMap.put(targetTick, eventList);
            }
            eventList.add(event);
        }
    }

    private static void triggerScheduledEvents() {
        synchronized (eventMap) {
            LinkedList<Event> eventList = eventMap.get(currentTick);
            if (eventList == null) return;
            Iterator<Event> events = eventList.iterator();
            while (events.hasNext()) {
                Event event = events.next();
                if (!event.isCancelled()) {
                    int nextDelay = event.trigger();
                    if (nextDelay > 0) {
                        scheduleEvent(event, nextDelay);
                    }
                }
                events.remove();
            }
            eventMap.remove(currentTick);
        }
    }

    public static Server server() {
        return server;
    }

}