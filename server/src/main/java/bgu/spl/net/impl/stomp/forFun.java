package bgu.spl.net.impl.stomp;
import java.util.*;

public class forFun {

    public static void main(String[] args) {

        System.out.println(1<<10);




        // String ans = "CONNECT\nID:208437277\nname:Ohad\n\nbody is null";
        // int lastIndexOfCommand = ans.indexOf("\n");
        // int firstIndexOfBody = ans.indexOf("\n\n");
        // System.out.println(lastIndexOfCommand);
        // System.out.println(firstIndexOfBody);

        // //for(int i = 0 ; i < ans.length() ; i++)
        //  //   System.out.println(ans.charAt(i) + " " + String.valueOf(i));
        

        // Command command = Command.valueOf(ans.substring(0, lastIndexOfCommand));
        // System.out.println(command.toString());


        // String body = ans.substring(firstIndexOfBody+2);

        // System.out.println(body);

        // String hashMapString = ans.substring(lastIndexOfCommand+1, firstIndexOfBody);
        // Map<String, String> h = new HashMap<>();

        // String[] hashMapArray = hashMapString.split("\n"); //split the string 
        // for(String substring : hashMapArray){
        //     int indexOfColon = substring.indexOf(":");
        //     if(indexOfColon == -1)
        //         break;
        //     h.put(substring.substring(0,indexOfColon), substring.substring(indexOfColon+1));
        // }
        // System.out.println(h);

        // // for(int i = 1; i < hashMapArray.length-2 ; i++){
        // //     int indexOfColon = messageString[i].indexOf(":");
        // //     if(indexOfColon == -1)
        // //         break;
        // //     h.put(messageString[i].substring(0,indexOfColon), messageString[i].substring(indexOfColon+1));
        // // }
        
        // if(body.equals("body is null"))
        //     body = null;
        // System.out.println(body);

    }
}