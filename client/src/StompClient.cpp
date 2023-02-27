#include <stdlib.h>
#include <iostream>

#include "../include/ConnectionHandler.h"
#include "../include/StompProtocol.h"
#include "../include/User.h"


using namespace std;

void readFromServerRun(ConnectionHandler* connectionHandler, bool& ThreadShouldTerminate,bool& isLogIn,User* user,StompProtocol* stmpP){

	while(!ThreadShouldTerminate){
        std::string answer;
        if (!(*connectionHandler).getFrameAscii(answer,'\0')) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
			ThreadShouldTerminate = true;
            break;
        }
		// std::cout << "answer from server" <<endl;
        // std::cout << answer <<endl;
		//string command = answer.substr(0,answer.find('\n'));
		isLogIn = (*stmpP).processFromServer(answer);
		//shouldTerminate = isLogIn;
		// std::cout << "finish to process from server" <<endl;
		// std::cout << isLogIn <<endl;
	}
	std::cout << "Thread is done" <<endl;
}

int main(int argc, char *argv[]) {	
	 
    while(1){
		std::cerr << "New user: "  << std::endl;
		User user = User();
		bool isLogIn = bool(false);
		bool shouldTerminate = bool(false);
		bool ThreadShouldTerminate = bool(false);
		bool endProgram = bool(false);
		std::string host;
		short port;
		bool triedToLogIn = false;
		string msg;
		int i =0;

		while(!triedToLogIn){
			const short bufsize = 1024;
			char buf[bufsize];
			std::cin.getline(buf, bufsize);
			std::string line(buf);
			// line = "login 127.0.0.1:20004 ohad carmel";
			string command=line.substr(0,line.find(' ')); 
			
			if (command == "login"){
				string subString = line.substr(line.find(' ')+1);
				host = subString.substr(0,subString.find(':'));
				subString = line.substr(line.find(':')+1);
				string portStr = subString.substr(0,subString.find(' '));
				port = stoi(portStr);
				msg = line;
				triedToLogIn = true;
			}
		}

		ConnectionHandler connectionHandler(host, port);
		if (!connectionHandler.connect()) {
			std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
			return 1;
		}
		
		StompProtocol stmP = StompProtocol(user,connectionHandler);

		thread readFromServerThread(&readFromServerRun, &connectionHandler , ref(ThreadShouldTerminate) , ref(isLogIn), &user,&stmP);
		stmP.process(msg);

		
		while (!shouldTerminate){
			std::cout <<"waiting for command:"<<endl;
			const short bufsize = 1024;
			char buf[bufsize];
			std::cin.getline(buf, bufsize);
			std::string line(buf);


			string command=line.substr(0,line.find(' ')); 
			i++;
			if(!isLogIn){
				break;
			}
			//   Do if the client already logged in with user.
			if (command == "login"){
				std::cout << "The client is already logged in, log out before trying again" << endl;
			}

			else if( line != "e"){
				stmP.process(line);
			}
			
			if(line == "e"){
				string command="logout";
				stmP.process(command);
				shouldTerminate = true;
				endProgram = true;
			}
			if (command == "logout"){
				shouldTerminate = true;
				
			}
			if(command == "print"){
				user.print();
			}
		}
		//std::cout << "shouldTerminate = true" << endl;
		//if(ThreadShouldTerminate!=nullptr)std::cout << "shouldTerminate = true" << endl;
		
		readFromServerThread.join();
		user.removeAllTopics();
		if(endProgram) break;
    	//delete &readFromServerThread;
	}
    
	return 0;
}



//