package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.LinkedList;;


public class ConnectionsImpl<T> implements Connections<T> {
    int currConnId = 0;
    private static ConnectionsImpl connections; 
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> connectionIdToConnectionHandler; //   connection id to connectionHandler
    private ConcurrentHashMap<String, User> registered; //   username as a string to object user
    private ConcurrentHashMap<Integer,User> clientToUser; //   connection ID to User
    //private ConcurrentHashMap<String, BlockingQueue <T>> topics; //(???????? צריך להיות כאן)
    //private ConcurrentHashMap<String, LinkedList<User>> channelToUsers; //(???????? צריך להיות כאן)
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer,Integer>> channelToConnectionIDToSubId;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer,String>> ConnectionIDToSubToIdchannel;
    private int messageId = 0;

    //private ConcurrentHashMap<String, LinkedList<Integer>> channelToSubID; // מפה שממיינת ערוץ למפה של 

//לאותו קלייט אסור שיהיו שני סאבאיידי זהים לשני ערוצים שונים.

    private ConnectionsImpl(){
        connectionIdToConnectionHandler = new ConcurrentHashMap<>();
        registered = new ConcurrentHashMap<>();
        clientToUser = new ConcurrentHashMap<>();
        //channelToUsers = new ConcurrentHashMap<>();
        channelToConnectionIDToSubId = new ConcurrentHashMap<>();
        ConnectionIDToSubToIdchannel = new ConcurrentHashMap<>();
    }
    
    public static synchronized ConnectionsImpl<?> getInstance(){
        if (connections == null)
            connections = new ConnectionsImpl<>();
        return connections;
    }
    
    public boolean send(int connectionId, T msg){
        System.out.println(msg.toString());
        ConnectionHandler<T> userConnectionHandler = connectionIdToConnectionHandler.get(connectionId);
        if(userConnectionHandler == null)
            return false;
        userConnectionHandler.send(msg);
        return true;
    }

    public void send(String channel, T msg){
        ConcurrentHashMap<Integer,Integer> channelMap = channelToConnectionIDToSubId.get(channel);
        if(channelMap != null){
            for (Integer key : channelMap.keySet()){ //every key is a connectionID
                if(clientToUser.get(key).isLoggedIn()){
                    System.out.println("key: " + String.valueOf(key));
                    System.out.println(clientToUser.get(key).isLoggedIn());
                    System.out.println(connectionIdToConnectionHandler.get(key));
                    connectionIdToConnectionHandler.get(key).send(msg);
                }

            }
        }
    }

    public synchronized void disconnect(int connectionId){
        User user = clientToUser.get(connectionId);
        user.setStatus(false);
        user.setConnId(-1);
        clientToUser.remove(connectionId);
        connectionIdToConnectionHandler.remove(connectionId);
        ConnectionIDToSubToIdchannel.remove(connectionId);
        for(String key :channelToConnectionIDToSubId.keySet() ){
            channelToConnectionIDToSubId.get(key).remove(connectionId);
        }
    }

    public synchronized boolean isRegistered(String username){
        if (registered.get(username) == null)
            return false;
        return true;
    }

    public synchronized void addToRegistered( String userName, String password, Integer connId){
        User user = new User(userName, password, connId);
        registered.put(userName, user);
        clientToUser.put(connId,user);
    }

    public synchronized void logIn(User user, int connId){
        user.logIn(connId);
        clientToUser.put(connId,user);
    }

    public synchronized boolean checkPassword (String username, String password){
        return (registered.get(username)).getPassword().equals(password);
    }

    public synchronized boolean isLoggedIn (User user){
        if (user == null)
            return false;
        return user.isLoggedIn();
    }

    public synchronized User getUser (int connId){
        return clientToUser.get(connId);
    }

    public synchronized User getUserByName (String username){
        return registered.get(username);
    }

    public synchronized void addConnectionHandler(Integer connId, ConnectionHandler CH){
        connectionIdToConnectionHandler.put(connId,CH);
    }

    public synchronized void subscribeToChannel(String channel, int connectionID, int subID){//(String channel, int connectionID, int subID)
        ConcurrentHashMap<Integer,Integer> channelMap = channelToConnectionIDToSubId.get(channel);
        if(channelMap == null){
            channelMap = new ConcurrentHashMap<>();
            channelMap.put(connectionID, subID);
            channelToConnectionIDToSubId.put( channel, channelMap);
        }
        else{
            channelMap.put(connectionID, subID);
        }
        ConcurrentHashMap<Integer,String> SubIdTochannelMap =ConnectionIDToSubToIdchannel.get(connectionID);
        if(SubIdTochannelMap == null){
            SubIdTochannelMap = new ConcurrentHashMap<>();
            SubIdTochannelMap.put(subID, channel);
            ConnectionIDToSubToIdchannel.put( connectionID, SubIdTochannelMap);
        }
        else{
            SubIdTochannelMap.put(subID, channel);
        }
    }    

    public synchronized boolean unSubscribeToChannel(int connectionID, int subID){
        ConcurrentHashMap<Integer,String> subIDtoChannel = ConnectionIDToSubToIdchannel.get(connectionID);
        if(subIDtoChannel==null || subIDtoChannel.get(subID)==null){
            return false; // If the connectionID doesn't have any subscriptions or the subId doesn't exist.
        }

        String channel = subIDtoChannel.get(subID); 
        subIDtoChannel.remove(subID);

        ConcurrentHashMap<Integer,Integer> channelMap = channelToConnectionIDToSubId.get(channel);
        channelMap.remove(connectionID);
        return true;
    }     
    
    public synchronized boolean isSubscribed(String channel, Integer connectionID){
        ConcurrentHashMap<Integer,Integer> channelMap = channelToConnectionIDToSubId.get(channel);
        if(channelMap != null && channelMap.get(connectionID)!=null)
            return true;
        return false;
    }

    public synchronized String getMessageId(){
        return String.valueOf(++messageId);
    }

    public synchronized int getSubId(String channel, int connId){
        return channelToConnectionIDToSubId.get(channel).get(connId);
    }

}
