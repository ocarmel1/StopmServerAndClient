#include "../include/User.h"

using std:: string;

User::User(): channels(), receipts(),subCtr(0),recCtr(0),subIdToEvents(), channelsToSubId(){};

User::~User(){}
User::User(const User& other): channels(other.channels),receipts(other.receipts),subCtr(other.subCtr),recCtr(other.recCtr),subIdToEvents(other.subIdToEvents){} //copyconstructor
User::User(User&& other):channels(other.channels),receipts(other.receipts),subCtr(other.subCtr),recCtr(other.recCtr),subIdToEvents(other.subIdToEvents){} //moveconstructor
User& User::operator=(const User& other){
    if (this != &other) {
        subCtr = other.subCtr;
        recCtr = other.recCtr;
        channels = other.channels;
        receipts = other.receipts;
        subIdToEvents =other.subIdToEvents;
    }
    return *this;

} //assingment operator
User& User::operator=(const User&& other){
    if (this != &other) {
            subCtr = other.subCtr;
            recCtr = other.recCtr;
            channels = other.channels;
            receipts = other.receipts;
            subIdToEvents =other.subIdToEvents;
        }
    return *this;
} //move assingment operator

void User::addChannel(int subId, string channel){
        if(channelsToSubId.find(channel)==channelsToSubId.end()){// if the channel doesnt exsist.
            channels.insert({subId, channel});
            channelsToSubId.insert({channel,subId});
            subId +=1;
        }
        else{
            if(subIdToEvents.find(channelsToSubId.find(channel) ->second)!=subIdToEvents.end()){
                list<string> l = subIdToEvents.find(channelsToSubId.find(channel) ->second) ->second; //copy the list of events
                subIdToEvents.erase(channelsToSubId.find(channel) ->second); //delete the old subId and events
                subIdToEvents.insert({subId,l});
            }
            channels.erase(channelsToSubId.find(channel) ->second);
            channels.insert({subId,channel});
            channelsToSubId.find(channel) ->second = subId;
        }
    }

void User::removeChannel(int subId){
        string channel = channels.find(subId)->second;
        channelsToSubId.erase(channel);
        channels.erase(subId);
        subIdToEvents.erase(subId);
    }

void User::removeChannel(string& channel){
    int subId = getSubscriptionId(channel);
    removeChannel(subId);
}

void User::addReceipt(int receiptId, string& receiptAction){
    receipts.insert({receiptId, receiptAction});
}

string User::getReceipt(int receiptId){
    string receipt = receipts.find(receiptId)->second;
    return receipt;
     //.find(receiptId);
}

string& User::getChannel(int subscriptionID){
    return channels.find(subscriptionID)->second;
}

int User::getSubscriptionId(string& channel){
    if(channelsToSubId.find(channel) != channelsToSubId.end())
        return channelsToSubId.find(channel)->second;
    return -1;
}

void User::removeAllTopics(){
     channels.clear();
     receipts.clear();
     channelsToSubId.clear();
     subIdToEvents.clear();
}

void User::setUserName(string username){
    userName = username;

}

string User::getUserName(){
    string username = userName;
    return username;
}

void User::addEvent(int subId, string e){
    auto itr =subIdToEvents.find(subId);
    if (itr != subIdToEvents.end()){
        (itr -> second).push_back(e);
    }
    else{
        list<string> l = list<string>();
        l.push_back(e);
        subIdToEvents.insert({subId,l});
    }
}

void User::print(){
    std::cout<<"user name: "<<endl;
    std::cout<< userName <<endl;
    std::cout<<"channels: "<<endl;
    map<int, string>::iterator itr;
    for(itr=channels.begin(); itr!=channels.end() ;itr++)
    {
        cout<<itr->first<<" "<<itr->second<<endl;
    }
    std::cout<<"receipts: "<<endl;
    for(itr=receipts.begin(); itr!=receipts.end() ;itr++)
    {
        cout<<itr->first<<" "<<itr->second<<endl;
    }
    std::cout<<"Events: "<<endl;
    map<int, list<string>>::iterator iter;
    for(iter=subIdToEvents.begin(); iter!=subIdToEvents.end() ;iter++)
    {   
        list<string> q = iter->second;
        list<string>::iterator listIter;
        for(listIter=q.begin(); listIter!=q.end() ;listIter++){
            cout<<iter->first<<" "<<(*listIter)<<endl;
        }        
    }
   
}