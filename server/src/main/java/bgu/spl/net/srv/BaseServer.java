package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.impl.stomp.ConnectionsImpl;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<StompMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private ServerSocket sock;
    private ConnectionsImpl<T> connections = (ConnectionsImpl<T>) ConnectionsImpl.getInstance();
    private int connIdCount = 1;

    public BaseServer(
            int port,
            Supplier<StompMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
        //connections = (ConnectionsImpl<T>) ConnectionsImpl.getInstance();
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Base server started");

            this.sock = serverSock; //just to be able to close
            //int connIdCount = 0;
            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();
                System.out.println("accept client");
                StompMessagingProtocol<T> protocol = protocolFactory.get();
                protocol.start(connIdCount, connections);
                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(
                        clientSock,
                        encdecFactory.get(),
                        protocol,
                        connIdCount);
                connections.addConnectionHandler(connIdCount, handler);
                connIdCount++;
                execute(handler);
                //connIdCount++;
            }
        } catch (IOException ex) {
            System.out.println("EXCEPTION");

            ex.printStackTrace();
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);
}
