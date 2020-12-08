package be.caedes.rs2.net.login;

import be.caedes.rs2.Event;
import be.caedes.rs2.Player;
import be.caedes.rs2.RS2World;
import be.caedes.rs2.net.Client;

public class LoginRequest extends Event implements Runnable {

    private final String username;
    private final String password;
    private final Client client;
    private Player player;

    public LoginRequest(Client client, String username, String password) {
        this.client = client;
        this.username = username;
        this.password = password;
    }

    //Runs on main thread as an Event
    @Override
    public int trigger() {
        if (!player.getPassword().equals(password)) {
            client.getOutBuffer().put((byte) LoginResponse.ERR_WRONG_USER_INFO);
            client.flushOutBuffer();
            //cancel client
            return 0;
        }
        if (player.isDisabled()) {
            client.getOutBuffer().put((byte) LoginResponse.ERR_ACCOUNT_DISABLED);
            client.flushOutBuffer();
            return 0;
        }
        return 0;
    }

    //Runs on IO thread
    @Override
    public void run() {
        player = Player.deserialize(username);
        RS2World.scheduleEvent(this);
    }
}
