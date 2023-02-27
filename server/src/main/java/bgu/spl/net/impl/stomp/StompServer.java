package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Server;

import java.io.IOException;


public class StompServer {
    

    public static void threadPerClient(int port) {
        Server.threadPerClient(
        port, //port
        () -> new StompProtocol<Message>(), //protocol factory
        StompEncoderDecoder::new //message encoder decoder factory
    ).serve();
      
    }


    public static void reactor(int port) {
        Server.reactor(
            10, 
            port, 
            () -> new StompProtocol<Message>() , 
            StompEncoderDecoder::new
            ).serve();
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        if (args.length > 1 && args[1].equals("reactor")) {
            reactor(port);
        } else {
            threadPerClient(port);
        }
    }
}