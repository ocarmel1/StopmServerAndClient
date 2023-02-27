package bgu.spl.net.impl.stomp;


 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;

import bgu.spl.net.impl.stomp.StompRCIClient;
import bgu.spl.net.impl.stomp.Message;




import java.util.Scanner;




public class StompClient {

    public static void main(String[] args) throws Exception {
        boolean isLogedIn = false;

        if (args.length == 0) {
            args = new String[]{"127.0.0.1"};
        }
        while(!Thread.currentThread().isInterrupted()){
            Scanner myObj = new Scanner(System.in);
            System.out.print("Enter command: ");
            String strCommand = myObj.nextLine();

            if(strCommand.equals("login") && !isLogedIn){
                Command command = Command.CONNECT;
                connectCleint(args[0]);
                isLogedIn = false;
            }

        }

        System.out.println("running clients");
        System.out.println("tring to subscrie");

        subscribe(args[0]);

        System.out.println("tring to unsubscrie");
        unsubscribe(args[0]);
        //runSecondClient(args[0]);
        //runThirdClient(args[0]);
    }

    private static void connectCleint(String host) throws Exception {
        try (StompRCIClient c = new StompRCIClient(host, 20004)) {
            HashMap<String, String> h = new HashMap<>();
            h.put("accept - version", "1.2");
            h.put("host", "stomp .cs. bgu.ac.il");
            h.put("login", "yuval");
            h.put("passcode", "films");

            Message msg = new Message(Command.CONNECT, h, host);
            //"CONNECT\naccept - version :1.2\nhost : stomp .cs. bgu.ac.il\nlogin : meni\npasscode : films\u0000"
            c.send(msg);
            
            System.out.println(c.receive()); //ok
            

            
        }

    }

    private static void send(String host) throws Exception {

    }

    private static void subscribe(String host) throws Exception {
        try (StompRCIClient c = new StompRCIClient(host, 7777)) {
            HashMap<String, String> h = new HashMap<>();
            h.put("destination", "/topic/a");
            h.put("id", "208437277");
            
            Message msg = new Message(Command.SUBSCRIBE, h, host);
            //"CONNECT\naccept - version :1.2\nhost : stomp .cs. bgu.ac.il\nlogin : meni\npasscode : films\u0000"
            c.send(msg);
            
            System.out.println(c.receive()); //ok
            
        }

    }

    private static void unsubscribe(String host) throws Exception {
        try (StompRCIClient c = new StompRCIClient(host, 7777)) {
            HashMap<String, String> h = new HashMap<>();
            h.put("id", "208437277");
            
            Message msg = new Message(Command.UNSUBSCRIBE, h, host);
            //"CONNECT\naccept - version :1.2\nhost : stomp .cs. bgu.ac.il\nlogin : meni\npasscode : films\u0000"
            c.send(msg);
            
            System.out.println(c.receive()); //ok
            
        }

    }
    
    private static void disconnect(String host) throws Exception {
        try (StompRCIClient c = new StompRCIClient(host, 7777)) {
            HashMap<String, String> h = new HashMap<>();
            h.put("accept - version", "1.2");
            h.put("host", "stomp .cs. bgu.ac.il");
            h.put("login", "meni");
            h.put("passcode", "films");

            Message msg = new Message(Command.CONNECT, h, host);
            //"CONNECT\naccept - version :1.2\nhost : stomp .cs. bgu.ac.il\nlogin : meni\npasscode : films\u0000"
            c.send(msg);
            
                System.out.println(c.receive()); //ok
            
        }
    }
        




        // public static void main(String[] args) throws IOException {

        //     if (args.length == 0) {
        //         args = new String[]{"localhost", "hello"};
        //     }
    
        //     if (args.length < 2) {
        //         System.out.println("you must supply two arguments: host, message");
        //         System.exit(1);
        //     }
    
        //     //BufferedReader and BufferedWriter automatically using UTF-8 encoding
        //     try (Socket sock = new Socket(args[0], 7777);
        //             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        //             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))) {
    
        //         System.out.println("sending message to server");
        //         out.write("CONNECT\naccept - version :1.2\nhost : stomp .cs. bgu.ac.il\nlogin : meni\npasscode : films\u0000");
        //         //out.newLine();
        //         out.flush();
    
        //         System.out.println("awaiting response");
        //         String line = in.readLine();
        //         System.out.println("message from server: " + line);
        //     }
        // }
    
}
