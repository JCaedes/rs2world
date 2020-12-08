package be.caedes.rs2;

import java.io.*;

public class Player implements Serializable {

    public static final String SAVES_DIR = ".\\saves\\";

    public static final int RANK_PLAYER = 0;
    public static final int RANK_MODERATOR = 1;
    public static final int RANK_ADMINISTRATOR = 2;

    private String username;
    private String password;
    private int rank;
    private transient int uid;
    private transient int id;
    private boolean disabled;

    public Player(String username, String password, int uid) {
        setUsername(username);
        setPassword(password);
        setRank(RANK_PLAYER);
        setDisabled(false);
        setUid(uid);
    }

    public static Player deserialize(String username) {
        Player player = null;
        try {
            String dir = SAVES_DIR + username;
            FileInputStream fileInputStream = new FileInputStream(dir);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            player = (Player) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch(IOException | ClassNotFoundException ioe) {
            ioe.printStackTrace();
        }
        return player;
    }

    public void serialize() {
        try {
            String dir = SAVES_DIR + username;
            FileOutputStream fileOutputStream = new FileOutputStream(dir);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
