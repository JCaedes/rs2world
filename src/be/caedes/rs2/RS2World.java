package be.caedes.rs2;

import be.caedes.rs2.net.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class RS2World {

    private static final int PORT = 43594;
    private static final int BATCH_ACCEPT_ATTEMPTS = 5;
    private static final long BATCH_ACCEPT_PERIOD = 1000;

    private static int currentTick = 0;
    private static final HashMap<Integer, LinkedList<Event>> eventMap = new HashMap<>();
    private static ServerSocketChannel serverSocketChannel;
    private static Selector selector;
    private static final HashMap<SelectionKey, Client> clientMap = new HashMap<>();
    private static long lastBatchAccept = 0;

    public static void main(String[] args) {
        System.out.println("INFO: Attempting to open rs2 server on port: "+PORT);
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(43594));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        boolean running = true;
        while (running) {
            currentTick++;
            long start = System.currentTimeMillis();

            //start server logic here
            cycleNetworkLogic();
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

    private static void cycleNetworkLogic() {
        try {
            selector.selectNow();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                if (key.isValid()) {
                    if (key.isAcceptable() && Utility.elapsedMillis(lastBatchAccept, BATCH_ACCEPT_PERIOD)) {
                        lastBatchAccept = System.currentTimeMillis();
                        for (int i = 0; i < BATCH_ACCEPT_ATTEMPTS; i++) {
                            SocketChannel channel = serverSocketChannel.accept();
                            if (channel == null) break;
                            channel.configureBlocking(false);
                            SelectionKey newKey = channel.register(selector, SelectionKey.OP_READ);
                            clientMap.put(newKey, new Client(newKey));
                        }
                    } else if (key.isReadable()) {
                        Client client = clientMap.get(key);
                        if (client != null) {
                            client.decode();
                        } else {
                            key.cancel();
                        }
                    }
                }
                keys.remove();
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

}