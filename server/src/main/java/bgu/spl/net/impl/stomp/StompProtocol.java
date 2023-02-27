package bgu.spl.net.impl.stomp;

import bgu.spl.net.impl.stomp.ConnectionsImpl;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;
import java.util.HashMap;
import java.util.Map;


public class StompProtocol<T> implements StompMessagingProtocol<Message>{
    
    private boolean shouldTerminate;
    private int connectionId;
    //שיניתי מגט אינסטנס
    private ConnectionsImpl connections;
    

    public StompProtocol(){
        shouldTerminate = false;
    }
    /**
	 * Used to initiate the current client protocol with it's personal connection ID and the connections implementation
	**/
    public void start(int connectionId, Connections<Message> connections) {
        this.connectionId = connectionId;
        this.connections = (ConnectionsImpl<Message>)(connections);
        //this.connections = connections;

    }
    @Override
    public Message process(Message message) {
        //receive a message and translates it to actions
       
        Command c = ((Message) message).command();
        if(c == Command.CONNECT) return connect(message);
        else if(c == Command.DISCONNECT) return disconnect(message);
        else if(c == Command.SUBSCRIBE) return subscribe(message);
        else if(c == Command.UNSUBSCRIBE) return unsubscribe(message);
        else if(c == Command.SEND) return send(message);
        else return illegalCommandError(message);

    }
    
    private static Message connectedMessage() {
        Map<String, String> h = new HashMap<>();
        h.put("version","1.2");
        return new Message(Command.CONNECTED,h,null); 
    }
    
    private static Message receipt(Map<String, String> headers) {
        return new Message(Command.RECEIPT,headers,null); 
    }

    private static Message error(Map<String, String> headers, String body){
        return new Message(Command.ERROR,headers,body);
    }

    private Message illegalCommandError(Message msg) {
        //if the error is "illegal command"
        Map<String, String> h = new HashMap<>();
        String msgID = msg.headers().get("message-id");
        h.put("receipt-id", "message" + msgID);
        h.put("message","Illegal command - the command does not exist");
        String body = "The message:\n-----\n"+msg.toString()+"\n-----\n";
        return error(h,body);
    }

    private Message disconnect(Message message) {
        
        Map<String, String> h = message.headers();
        String receiptID = h.get("receipt");
        h.clear();
        h.put("receipt id" , receiptID);
        //connections.send(connectionId,receipt(h));
        connections.disconnect(connectionId);
        return receipt(h);

    }

    private Message send(Message message) {
        Map<String, String> h = message.headers();
        String channel = h.get("destination");
        if(channel == null){
            String body = "The message:\n-----\n"+message.toString()+"\n-----\n Did not contain a destination header,\n which is REQUIRED for message propagation.";
            h.clear();
            h.put("message","malformed frame received");
             
            return error(h,body);
        }
        else{
            //if the user is subscribed to the channel

            if(connections.isSubscribed(channel,connectionId)){
                //System.out.println(h);
                h.put("subscription ", String.valueOf(connections.getSubId(channel, connectionId)));//subscription :78
                //System.out.println(h);
                h.put("message-id ", connections.getMessageId());//message - id :20
                //System.out.println(h);
                //h.put("subscription ", String.valueOf(connections.getSubId(channel, connectionId)));//subscription :78
                //receipt
                String receiptID = h.get("receipt");    
                h.remove("receipt");
                System.out.println(h);
                //send the message
                connections.send(channel,new Message(Command.MESSAGE, h, message.body()));
                
                h.clear();
                h.put("receipt id" , receiptID);
                return receipt(h);


            } 
            else{
                //error
                String body = "The message:\n-----\n"+message.toString()+"\n-----\n You are not subscribed to the channel '"+channel+ "',\n which is REQUIRED in order to send message to the channel.";
                h.clear();
                h.put("message","user is not subscribed to the channel");
                return error(h,body);
            }
            
        }
        
    }

    private Message unsubscribe(Message message) {
        Map<String, String> h = message.headers();
        String subid = h.get("id");
        
        //if the header is not null
        
        int subID = Integer.valueOf(subid);
        boolean succeeded = connections.unSubscribeToChannel(connectionId,subID);
        
        //if the process went successfuly
        if(succeeded){
            //receipt
            String receiptID = h.get("receipt");
            h.clear();
            h.put("receipt id", receiptID);
            return receipt(h);
        }
        return error(h, null);
    }

    private Message subscribe(Message message) {
        Map<String, String> h = message.headers();
        String channel = h.get("destination");
        String subid = h.get("id");

        //if the headers are not full -> send error
        if(channel == null || subid == null){
            String body = "The message:\n-----\n"+message.toString()+"\n-----\n Did not contain a destination header or subscription id,\n which is REQUIRED for message propagation.";
            h.clear();
            h.put("message","malformed frame received ");
            return error(h,body);
        }
        
        else{
            int subID = Integer.valueOf(subid);
            String receiptID = h.get("receipt");
            h.clear();
            h.put("receipt id", receiptID);
            //if the user is not already subsribed to the channel -> send error
            if(!connections.isSubscribed(channel,connectionId)) {
                connections.subscribeToChannel(channel,connectionId,subID);             
                
                return receipt(h);
                // String body = "The message:\n-----\n"+message.toString()+"\n-----\n ";
                // h.clear();
                // h.put("message","The user is already subscribed to the channel ");
                // connections.send(connectionId,error(h,body));
            } 
            //else subscribe and send receipt
            else{
                return error(h, null);
            }
        }
        
    }

    private Message connect(Message message) {
        Map<String, String> h = message.headers();
        String userName = h.get("login");
        String password = h.get("passcode");
        User user = connections.getUserByName(userName);
        
        // /if the user does not exists
        if (user == null){
           
            //addToRegistered
            connections.addToRegistered(userName, password, connectionId);
            //get User
            user = connections.getUserByName(userName);
            //logIn
            connections.logIn(user,connectionId);
            //send CONNECTED frame
            Message connected = connectedMessage();
            return connected;
        }

        //if the user already exists
        else{
             //check if the user is logged in

            //the user is already logged in
            if(connections.isLoggedIn(user)){
                //err if so
                //לדבר עם אוהד על איזו שגיאה
                System.out.println("isLoggedIn");

                String body = "The message:\n-----\n"+message.toString()+"\n-----\n You have to disconnect before you re-connect";
                String rec = h.get("receipt");
                h.clear();
                if( rec != null) h.put("receipt id", rec);
                h.put("message","The user is already connected");
                Message error = new Message(Command.ERROR, h, body);
                System.out.println("error");
                System.out.println(error.toString());

                return error;
            }
            
            //the user is not logged in
            else{   //check password 
                System.out.println("check password ");

                //wrong password
                if(!(connections.checkPassword(userName, password))){
                    //error if so
                    String body = "The message:\n-----\n"+message.toString()+"\n-----\nTry another password";
                    h.clear();
                    h.put("message","Wrong password");
                    Message error = new Message(Command.ERROR, h, body);
                    return error;
                    ///connections.send(connectionId,error(h,body));
                }
                               
                //correct password
                else{
                    
                    //login
                    connections.logIn(user,connectionId);
                    //send CONNECTED frame
                    //connections.send(connectionId,connectedMessage());
                    return connectedMessage();
                }
            }
            
        }
        //return new Message(null, h, password);

    }

    @Override
    public boolean shouldTerminate() {
        
        return shouldTerminate;
    }
    /* (non-Javadoc)
     * @see bgu.spl.net.api.StompMessagingProtocol#start(int, bgu.spl.net.impl.stomp.ConnectionsImpl)
     */
    @Override
    public void start(int connectionId, ConnectionsImpl<Message> connections) {
        this.connectionId = connectionId;
        this.connections = (ConnectionsImpl<Message>)(connections);
        
    }

}   