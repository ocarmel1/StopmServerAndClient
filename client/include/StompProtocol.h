#pragma once
#include <string>
#include <iostream>
#include <fstream>
#include <vector>
#include "../include/ConnectionHandler.h"
#include "../include/User.h"
#include "../include/event.h"

//#include <boost/asio.hpp>
//class Message;
//using boost::asio::ip::tcp;
using namespace std;
// class User;
// TODO: implement the STOMP protocol
class StompProtocol
{

private:
    bool connected;
    User& user;
    vector<string> inputFromServer;
    ConnectionHandler& connectionhandler;



public:
    StompProtocol(User& us, ConnectionHandler& ch);
    virtual ~StompProtocol(); //destructor
    StompProtocol(const StompProtocol& other); //copyconstructor
    StompProtocol(StompProtocol&& other); //moveconstructor
    StompProtocol& operator=(const StompProtocol& other); //assingment operator
    StompProtocol& operator=(StompProtocol&& other); //move assingment operator


    //from client keyboard to server
    bool process(string& msg);
    bool send(const string &frameToServer); 
    bool processFromServer(string & msg);

};