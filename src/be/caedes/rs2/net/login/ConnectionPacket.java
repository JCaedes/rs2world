package be.caedes.rs2.net.login;

import be.caedes.rs2.RS2World;
import be.caedes.rs2.Utility;
import be.caedes.rs2.net.Client;
import be.caedes.rs2.net.ClientEvent;

import java.nio.ByteBuffer;

public class ConnectionPacket implements ClientEvent {

    private int requestType;

    private ConnectionPacket() {

    }

    public static ConnectionPacket build(ByteBuffer buffer) {
        if (buffer.remaining() < 2) return null;
        ConnectionPacket packet = new ConnectionPacket();
        packet.requestType = buffer.get() & 0xff;
        return packet;
    }

    @Override
    public void trigger(Client client) {
        if (requestType != 14) {
            client.sendError(LoginResponse.ERR_REJECTED_SESSION);
            return;
        }
        client.getOutBuffer().putLong(0);
        client.getOutBuffer().put((byte) LoginResponse.CONNECTION_ACCEPTED);
        client.getOutBuffer().putLong(Utility.rng.nextLong());
        client.flushOutBuffer();
        client.setState(Client.ClientState.DECODING);
    }

}
