package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.MessageEncoderDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;


public class StompEncoderDecoder implements MessageEncoderDecoder<Message> {
    
    private byte[] bytes = new byte[1 << 10]; //start with 1024.
    private int len = 0;
    //private int start = 0;

    @Override
    public Message decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if (nextByte == '\u0000') {
            String s = popString();
            return stringToMessage(s);
        }


        pushByte(nextByte);
        return null; //not a line yet
    }

    @Override
    public byte[] encode(Message message1 ) {
        //String s = "";//"/n/n"[command,header1:value,...,"",body]
        
        Command c = message1.command();
        Map<String,String> h = message1.headers();
        String b = message1.body(); 

        //java.io.OutputStream out 
        StringBuffer message = new StringBuffer(c.toString());
        message.append( "\n" );

        if (h != null) {
            for (Iterator keys = h.keySet().iterator(); keys.hasNext(); ) {
                String key = (String)keys.next();
                String value = (String)h.get(key);
                message.append( key );
                message.append( ":" );
                message.append( value );
                message.append( "\n" );
            }
        }
        message.append( "\n" );

        if (b != null) message.append( b );
        //else message.append("body is null");

        message.append( "\0" );


        return (message1.toString()+"\u0000").getBytes(); //uses utf8 by default
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        return result;
    }

    private Message stringToMessage(String popString){

        //String ans = "CONNECT\nID:208437277\nname:Ohad\n\nbody is null";
        //System.out.println(popString);
        int lastIndexOfCommand = popString.indexOf("\n"); 
        int firstIndexOfBody = popString.indexOf("\n\n");

        Command command = Command.valueOf(popString.substring(0, lastIndexOfCommand));

        String body = popString.substring(firstIndexOfBody+2);


        String hashMapString = popString.substring(lastIndexOfCommand+1, firstIndexOfBody);
        Map<String, String> h = new HashMap<>();

        String[] hashMapArray = hashMapString.split("\n"); //split the string 
        for(String substring : hashMapArray){
            int indexOfColon = substring.indexOf(":");
            // if(indexOfColon == -1)
            //     break;
            //System.out.println(substring.substring(0,indexOfColon)+" --" + substring.substring(indexOfColon+1));
            h.put(substring.substring(0,indexOfColon), substring.substring(indexOfColon+1));
        }

        if(body.equals("body is null"))
            body = null;
        return new Message(command, h, body);        
    }
}