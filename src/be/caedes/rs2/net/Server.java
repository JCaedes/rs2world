package be.caedes.rs2.net;

import be.caedes.rs2.FixedArrayList;
import be.caedes.rs2.Utility;
import be.caedes.rs2.net.login.ConnectionPacket;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.Tree;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

public final class Server implements Runnable {

    private static final int BUFFER_SIZE = 1024;
    private static final int BATCH_ACCEPT_ATTEMPTS = 5;
    private static final long BATCH_ACCEPT_PERIOD = 1000;

    private final ByteBuffer buffer;
    private final FixedArrayList<Client> clients;
    private final HashMap<SelectionKey, Client> clientMap;
    private final HashMap<String, Client> nameMap;
    private final HashMap<Integer, Client> uidMap;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private long lastBatchAccept;

    public Server(int port, int capacity) {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress("127.0.0.1", port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        lastBatchAccept = 0;
        clients = new FixedArrayList<>(new Client[capacity]);
        clientMap = new HashMap<>();
        nameMap = new HashMap<>();
        uidMap = new HashMap<>();
    }

    @Override
    public void run() {
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
                            synchronized (clientMap) {
                                clientMap.put(newKey, new Client(newKey));
                            }
                        }
                    } else if (key.isReadable()) {
                        Client client = clientMap.get(key);
                        if (client != null) {
                            buffer.clear();
                            int bytesRead = client.getChannel().read(buffer);
                            if (bytesRead != -1) {
                                buffer.flip();
                                ClientEvent packet;
                                synchronized (client.getPacketQueue()) {
                                    while ((packet = buildPacket(client)) != null) {
                                        client.getPacketQueue().add(packet);
                                    }
                                }
                            } else {
                                deregister(client);
                            }
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

    public void deregister(Client client) {
        try {
            client.getChannel().close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            client.getKey().cancel();
        }
        synchronized (clientMap) {
            clientMap.remove(client.getKey());
        }
        if (client.getPlayer() != null) {
            synchronized (nameMap) {
                nameMap.remove(client.getPlayer().getUsername());
            }
            synchronized (uidMap) {
                uidMap.remove(client.getPlayer().getUid());
            }
            synchronized (clients) {
                clients.set(client.getPlayer().getId(), null);
            }
        }
    }

    private ClientEvent buildPacket(Client client) {
        switch (client.getState()) {
            case CONNECTING:
                return ConnectionPacket.build(buffer);
            case DECODING:
        }
        return null;
    }

}
