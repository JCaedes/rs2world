package be.caedes.rs2.net;

import be.caedes.rs2.Player;
import be.caedes.rs2.RS2World;
import be.caedes.rs2.Utility;
import be.caedes.rs2.net.login.LoginResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public final class Client {

    private static final int BUFFER_SIZE = 1024;

    public enum ClientState {
        CONNECTING,
        DECODING,
        CONNECTED
    }

    private final SelectionKey key;
    private final SocketChannel channel;
    private final ByteBuffer outBuffer;
    private final LinkedList<ClientEvent> packetQueue;
    private ClientState state;
    private ISAACCipher encryptor;
    private ISAACCipher decryptor;
    private Player player;

    public Client(SelectionKey key) {
        this.key = key;
        this.channel = (SocketChannel) key.channel();
        this.outBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.state = ClientState.CONNECTING;
        this.packetQueue = new LinkedList<>();
    }

    public void decode(ByteBuffer buffer) {
        try {
            buffer.clear();
            int bytesRead = channel.read(buffer);
            if (bytesRead == -1) {
                //cancel this client
                return;
            }
            if (bytesRead >= BUFFER_SIZE) {
                System.out.println("WARNING: incoming packet size was equal or larger than internal buffer for "+
                        "client: "+this);
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
            //cancel this client
        }
        buffer.flip();
        switch(state) {
            case CONNECTING:
                if (buffer.remaining() < 2) {
                    return;
                }
                int connectionRequest = buffer.get() & 0xff;
                if (connectionRequest != 14) {
                    System.out.println("ERROR: Invalid connection request id: " + connectionRequest);
                    //cancel this client
                    return;
                }
                outBuffer.putLong(0);
                outBuffer.put((byte) 0);
                outBuffer.putLong(Utility.rng.nextLong());
                flushOutBuffer();
                state = ClientState.DECODING;
                break;
            case DECODING:
                if (buffer.remaining() < 2 ) {
                    return;
                }
                int loginRequest = buffer.get() & 0xff;
                boolean reconnect = loginRequest == LoginResponse.RECONNECT_REQUEST;
                if (loginRequest != 16 && loginRequest != 18) {
                    System.out.println("ERROR: Invalid login request id: " + loginRequest);
                    //cancel this client
                    return;
                }
                int loginLength = buffer.get() & 0xff;
                if (buffer.remaining() < loginLength) return;
                buffer.get();
                int clientVersion = buffer.getShort();
                buffer.get();
                for (int i = 0; i < 9; i++) buffer.getInt();
                buffer.getShort();
                long csk = buffer.getLong();
                long ssk = buffer.getLong();
                int[] seed = {
                        (int)(csk >> 32),
                        (int)csk,
                        (int)(ssk >> 32),
                        (int)ssk
                };
                decryptor = new ISAACCipher(seed);
                for (int i = 0; i < seed.length; i++) seed[i] += 50;
                encryptor = new ISAACCipher(seed);
                int uid = buffer.getInt();
                String username = BufferOps.readString(buffer);
                String password = BufferOps.readString(buffer);
                //now perform validation

                break;
            case CONNECTED:
                break;
        }
    }

    public void sendError(int message) {
        outBuffer.put((byte) message);
        flushOutBuffer();
        RS2World.server().deregister(this);
    }

    public ClientState getState() {
        return state;
    }

    public void setState(ClientState state) {
        this.state = state;
    }

    public ISAACCipher getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(ISAACCipher encryptor) {
        this.encryptor = encryptor;
    }

    public ISAACCipher getDecryptor() {
        return decryptor;
    }

    public void setDecryptor(ISAACCipher decryptor) {
        this.decryptor = decryptor;
    }

    public LinkedList<ClientEvent> getPacketQueue() {
        return packetQueue;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public ByteBuffer getOutBuffer() {
        return outBuffer;
    }

    public void flushOutBuffer() {
        try {
            outBuffer.flip();
            if (outBuffer.remaining() < 0) {
                outBuffer.clear();
                return;
            }
            channel.write(outBuffer);
            outBuffer.clear();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public SelectionKey getKey() {
        return key;
    }

}