package be.caedes.rs2.net;

import java.nio.ByteBuffer;

public class BufferOps {

    public static String readString(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder();
        byte b;
        while ((b = buffer.get()) != 10) {
            sb.append((char) b);
        }
        return sb.toString();
    }

}
