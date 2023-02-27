package bgu.spl.net.impl.stomp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public class StompRCIClient implements Closeable {

    private final StompEncoderDecoder encdec;
    private final Socket sock;
    private final BufferedInputStream in;
    private final BufferedOutputStream out;

    public StompRCIClient(String host, int port) throws IOException {
        sock = new Socket(host, port);
        encdec = new StompEncoderDecoder();
        in = new BufferedInputStream(sock.getInputStream());
        out = new BufferedOutputStream(sock.getOutputStream());
    }

    public void send(Message cmd) throws IOException {
        out.write(encdec.encode(cmd));
        out.flush();
    }

    public Message receive() throws IOException {
        int read;
        while ((read = in.read()) >= 0) {
            Message msg = encdec.decodeNextByte((byte) read);
            if (msg != null) {
                return msg;
            }
        }

        throw new IOException("disconnected before complete reading message");
    }

    @Override
    public void close() throws IOException {
        out.close();
        in.close();
        sock.close();
    }

}
