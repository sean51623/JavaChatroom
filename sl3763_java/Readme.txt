***PA1 Readme***
Name: Sung-Yen Liu
UNI: sl3763

1.
Server - one Server Socket, most information maintain by HashMap
	ServerSocket serverSoc // main serverSocket
	HashMap<String, String> account // record <username, password> pairs in credentials.txt
	HashMap<String, User> login // record who is online
	ArrayList<String> blockList // record which ID is blocked because of fail to login
	HashMap<String, HashMap<String, ArrayList<String>>> offlineMSG // save offline message
	HashMap<String, Integer> blockUser // record the record of login
	HashMap<String, ArrayList<String>> blackList // record who is blocked by other users

Client - one Server Socket, two thread (Keyboard, Server)
	HashMap<String, User2> contactList // record the contact (ip, port) of different clients
	User2 is a data structure to maintain user login information {username, ip, port}

2. How to run/compile my source code? (Two ways)
(1) By simply type the command "make" (using makefile)
(2) Since there are only two files, just compile both of them
	- javac Server.java
	- javac Client.java
(3) Run
	- java Server [port_number]
	- java Client [IP] [port_number]

3. Sample command
*1. Implemented command:
	(1) login
	(2) broadcast <message>
	(3) message <user> <message> (& offline message when receiver is not online)
	(4) online
	(5) block <user>, unblock <user>
	(6) getaddress <user>
	(7) private <user> <message>
	(8) logout

*2. Some parts that are not working so well:
	(1) login - blocking time (if fail to login with same username more than 3 times)
	(2) private - private connection works fine, but cannot promise to hide the message from users been blocked
	(3) Can not change private message to offline message (routing through server) if receiver is not online
	
*3 Fail to implement
	(1) Heartbeat mechanism