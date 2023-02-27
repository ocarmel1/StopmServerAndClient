#pragma once
#include <string>
#include <iostream>
#include <map>
#include <queue>
#include <list>
#include "../include/ConnectionHandler.h"
#include "event.h"
#include "SafeQueue.h"




using namespace std;

class User{
private:
    
    

public:
    string userName;
    
    map<int, string> channels; // sub-id to channel.
    map<int, string> receipts; // receip-od to string.
    map<string, int> channelsToSubId;
    map<int, list<string>> subIdToEvents; 
    //    channels.insert(std::pair<int, string>(1, "Chen Long"));  
    int subCtr;
    int recCtr;

    User();
    virtual ~User(); //destructor
    User(const User& other); //copyconstructor
    User(User&& other); //moveconstructor
    User& operator=(const User& other); //assingment operator
    User& operator=(const User&& other); //move assingment operator
    void addChannel(int subId, string channel);
    void removeChannel(int subId);
    void removeChannel(string& channel);
    void addEvent(int subId, string e);
    void addReceipt(int receiptId, string& receiptAction);
    string getReceipt(int receiptId);
    string& getChannel(int subscriptionID);
    int getSubscriptionId(string& channel);
    void removeAllTopics();
    void setUserName(string username); 
    string getUserName(); 
    void print();
    
};