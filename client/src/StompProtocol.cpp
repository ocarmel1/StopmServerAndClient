#include "../include/StompProtocol.h"
#include <iostream>

StompProtocol::StompProtocol(User& us,ConnectionHandler& ch ):  connected(false), user(us), connectionhandler(ch), inputFromServer() {}
//destructor
StompProtocol::~StompProtocol(){}
//copyconstructor
StompProtocol::StompProtocol(const StompProtocol& other):connected(other.connected), user(other.user) , connectionhandler(other.connectionhandler),inputFromServer(other.inputFromServer)  {} 
//moveconstructor
StompProtocol::StompProtocol(StompProtocol&& other):connected(other.connected), user(other.user) , connectionhandler(other.connectionhandler),inputFromServer(other.inputFromServer)  {} 
//assingment operator
StompProtocol& StompProtocol::operator=(const StompProtocol& other)
{
    if(this == &other) return *this;
    
    StompProtocol stmp(other.user, other.connectionhandler);
    stmp.connected = other.connected;
    stmp.inputFromServer = other.inputFromServer;
    *this = stmp;
    stmp.~StompProtocol();
    return *this;
    
}
//move assingment operator
StompProtocol& StompProtocol::operator=( StompProtocol&& other)
{
    if(this!= &other){
        StompProtocol stmp(other.user, other.connectionhandler);
        stmp.connected = move(other.connected);
        stmp.inputFromServer = move(other.inputFromServer);
        *this = stmp;
        stmp.~StompProtocol();
        other.~StompProtocol();
    }
    return *this;
}


//from client keyboard to server
bool StompProtocol:: process(string& msg){
    string command = msg.substr(0,msg.find(' '));

    if(command == "login"){
        //connect
        string login = msg.substr(22);
        string password = login.substr(login.find(" ")+1);
        string username = login.substr(0, login.find(" "));
        user.setUserName(username);
        login = "CONNECT\naccept-version:1.2\nhost:stomp.cs.bgu.ac.il\nlogin:";
        login.append(username);
        login.append("\npasscode:");
        login.append(password);
        login.append("\n\n");
        return send(login);
    }
    
    if(command == "join"){

        //subscribe
        string channel(msg.substr(msg.find(' ')+1));
        string subscribe = "SUBSCRIBE\ndestination:";
        subscribe.append(channel+"\n"+"id:"+ std::to_string(user.subCtr)+"\n"+"receipt:"+ std::to_string(user.recCtr)+"\n\n");
        string receiptStr = "subscribe to"+channel;
        user.addReceipt(user.recCtr,msg);
        //user.addChannel(user.subCtr, channel); //if error with this receipt id-> removechannel
        //user.subCtr++;
        user.recCtr++;
        // cout<<"join fram:"<<endl;
        // cout<<subscribe<<endl;
        return send(subscribe);

    }
    
    if(command == "exit"){
        //unsubscribe
        string channel(msg.substr(msg.find(' ')+1));
        string unsubscribe = "UNSUBSCRIBE\nid:"+std::to_string(user.getSubscriptionId(channel))+"\nreceipt:"+std::to_string(user.recCtr)+"\n\n";
        user.addReceipt(user.recCtr,msg);
        user.recCtr++;

        return send(unsubscribe);
    }
    
    if(command == "report"){
        string reportPath = msg.substr(msg.find(' ')+1);

        names_and_events namesAndEvents = parseEventsFile(reportPath, user.getUserName());
        string teamA = namesAndEvents.team_a_name;
        string teamB = namesAndEvents.team_b_name;
        string eventFrame;
        for(auto event : namesAndEvents.events){
            string username = user.getUserName();
            eventFrame.append("SEND\n");
            eventFrame.append("destination:"+event.get_team_a_name()+"_"+event.get_team_b_name());
            eventFrame.append("\nreceipt:"+std::to_string(user.recCtr)+"\n\n");
            eventFrame.append("user:"+event.get_sender()+"\n");
            eventFrame.append("team a:"+event.get_team_a_name()+"\n");
            eventFrame.append("team b:"+event.get_team_b_name()+"\n");
            eventFrame.append("event name:"+event.get_name()+"\n");
            eventFrame.append("time:"+std::to_string(event.get_time()) + "\n");
        
            eventFrame.append("general game updates:\n");
            for(auto itr : event.get_game_updates())
                eventFrame.append( itr.first + ":" + itr.second + "\n");
            
            eventFrame.append("team a updates:\n");
            for(auto itr : event.get_team_a_updates())
                eventFrame.append( itr.first + ":" + itr.second + "\n");
            eventFrame.append("team b updates:\n");
            for(auto itr : event.get_team_b_updates())
                eventFrame.append( itr.first + ":" + itr.second + "\n");
            eventFrame.append("description:"+ event.get_discription()+"\n");
            //std::cout << eventFrame <<endl;
            user.addReceipt(user.recCtr,eventFrame);
            user.recCtr++;
            send(eventFrame);
            eventFrame.clear();
        } 
    }

    if(command == "summary"){
        string subString = msg.substr(msg.find(' ')+1); //{game_name} {user} {file}  
        string gameName = subString.substr(0,subString.find(' ')); //{game_name}
        subString = subString.substr(subString.find(' ')+1); //{user} {file}
        string senderName = subString.substr(0,subString.find(' '));//{user}
        string fileName = subString.substr(subString.find(' ')+1); //{file}
        
        string GameGeneralStats = gameName.substr(0,gameName.find('_')) + " vs " +gameName.substr(gameName.find('_')+1) +"\n"+"Game stats:\nGeneral stats:\n";
        string TeamAstats = gameName.substr(0,gameName.find('_')) + " stats:\n" ;
        string TeamBstats = gameName.substr(gameName.find('_')+1) + " stats:\n" ;
        string GameEventsReports = "Game event reports:\n";
        int subIdToChannel = user.channelsToSubId.find(gameName)->second;

        std::cout<<"Events: "<<endl;
        //std::cout<<GameGeneralStats<<endl;
        map<int, list<string>>::iterator iter;
          
            list<string> q = user.subIdToEvents.find(subIdToChannel)->second;
            list<string>::iterator listIter;
            for(listIter=q.begin(); listIter!=q.end() ;listIter++){
                string name = (*listIter).substr((*listIter).find(':')+1 , (*listIter).find('\n',2)-(*listIter).find(':')-1);  
                if(senderName == name){
                    //cout<<(*listIter)<<endl;
                    string eventName = (*listIter).substr((*listIter).find("event name:")+11,(*listIter).substr((*listIter).find("event name:")).length()-12 - (*listIter).substr((*listIter).find("time:")).length());
                    string eventTime = (*listIter).substr((*listIter).find("time:")+5,(*listIter).substr((*listIter).find("time:")).length()-5 - (*listIter).substr((*listIter).find("time:")).length());
                    string eventDescription = (*listIter).substr((*listIter).find("description:")+12);
                    
                    string TeamAupdates = (*listIter).substr((*listIter).find("team a updates:")+15,(*listIter).substr((*listIter).find("team a updates:")).length()-16 - (*listIter).substr((*listIter).find("team b updates:")).length());
                    string TeamBupdates = (*listIter).substr((*listIter).find("team b updates:")+15,(*listIter).substr((*listIter).find("team b updates:")).length()-16 - (*listIter).substr((*listIter).find("description:")).length());
                    
                    string generalStats = (*listIter).substr((*listIter).find("general game updates:")+21,(*listIter).substr((*listIter).find("general game updates:")).length()-21 - (*listIter).substr((*listIter).find("team a updates:")).length());

                    GameGeneralStats.append(generalStats);
                    TeamAstats.append(TeamAupdates+"\n");            
                    TeamBstats.append(TeamBupdates+"\n");
                    GameEventsReports.append(eventTime + " - " + eventName);

                    //cout<<eventDescription<<endl;


                }
            }    
               GameGeneralStats.append(TeamAstats) ;
               GameGeneralStats.append(TeamBstats) ;
               GameGeneralStats.append(GameEventsReports) ;
               cout<<GameGeneralStats<<endl;
        
            // Create and open a text file
            ofstream MyFile(fileName);

            // Write to the file
            MyFile << GameGeneralStats;

            // Close the file
            MyFile.close();

    }

    if(command == "logout"){
        //disconnect
        string disconnect = "DISCONNECT\nreceipt:"+std::to_string(user.recCtr)+"\n\n";
        user.addReceipt(user.recCtr,msg);
        //user.addReceipt(user.recCtr,disconnect);
        user.recCtr++;
        
        return send(disconnect);
    }

}

bool StompProtocol:: processFromServer(string& msg){
 
    string command = msg.substr(0,msg.find('\n'));
    

    //message
    if(command == "MESSAGE"){
        
        string SubIdStr = "subscription :";
        string msgIdStr = "message-id :";
        string subIdStr = msg.substr(msg.find(SubIdStr)+SubIdStr.length(),msg.find(msgIdStr));
        int subId = stoi(subIdStr);
        string body = msg.substr(msg.find("\n\n"));
        user.addEvent(subId,body);
        return true;
    }

    //receipt
    else if(command == "RECEIPT"){
        //RECEIPT\nreceiptId:0\n\n
        string receipStr = msg.substr(msg.find(':')+1);
        
        int receiptId = std::stoi(receipStr.substr(0,msg.find('\n')));
         
        string action = user.receipts.find(receiptId)->second;
        
        command = action.substr(0,action.find(' '));
        if(command == "join"){
            std::cout << "Joined channel " + action.substr(action.find(' ')+1) << endl;
            string channel = action.substr(action.find(' ')+1);
            user.addChannel(user.subCtr, channel); //if error with this receipt id-> removechannel
            user.subCtr++;
        }
        if (command == "exit"){
            string channel = action.substr(action.find(' ')+1);
            std::cout << "Exited channel " + action.substr(action.find(' ')+1) << endl;
            //user.removeChannel(channel);
            return true;

        }
        
        else if (command == "logout"){
            //clear all subscriptions
            user.removeAllTopics();
            //*****************
            // צריך לשנות את המשתנה הבוליאני של הקליינט 
            //******************
            // close socket
            connectionhandler.close();
            return false;

        }
    }

    else if(command == "ERROR"){
        // cout << "the message:" <<endl;
        // cout << msg <<endl;
        // int receiptId = std::stoi(msg.substr(msg.find(':'+10, '\n'))); //receipt - id : message-12345
        // cout << receiptId<<endl;
        // string action = user.getReceipt(receiptId);
        // command = action.substr(action.find(' '));
            
        cout <<  msg << endl;
        connectionhandler.close();
        return false;
        }   
    
    else if(command == "CONNECTED"){
        std::cout << "Login successful" <<endl;
        return true;
    }
    return true;
}

bool StompProtocol::send(const string  &frameToServer){
    return connectionhandler.sendFrameAscii(frameToServer, '\u0000');
}

