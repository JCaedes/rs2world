package be.caedes.rs2.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public final class Client {

    public enum ClientState {
        CONNECTING,
        VALIDATING,
        CONNECTED
    }

    private SelectionKey key;
    private SocketChannel channel;
    private ByteBuffer inBuffer;
    private ByteBuffer outBuffer;
    private ClientState state;

    public Client(SelectionKey key) {
        this.key = key;
        this.channel = (SocketChannel) key.channel();
        this.inBuffer = ByteBuffer.allocate(1024);
        this.outBuffer = ByteBuffer.allocate(1024);
        this.state = ClientState.CONNECTING;
    }

    public void decode() {
        try {
            inBuffer.clear();
            int bytesRead = channel.read(inBuffer);
            if (bytesRead == -1) {
                //cancel this client
                return;
            }
            inBuffer.flip();
            switch(state) {
                case CONNECTING:
                    break;
                case VALIDATING:
                    break;
                case CONNECTED:
                    break;
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public ClientState getState() {
        return state;
    }

    public void setState(ClientState state) {
        this.state = state;
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

}
