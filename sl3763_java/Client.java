// new PA1

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
	ServerSocket cliSerSoc;
	String pattern;
	Socket clientSoc;
	String username;
	String password;
	String ip;
	int serverPort;
	int localPort;
	boolean isConnect;
	HashMap<String, User2> contactList = new HashMap<>();;
	
	public class User2 {
		String username;
		String ip;
		int port;
		public User2 (String username, String ip, int port) {
			this.username = username;
			this.ip = ip;
			this.port = port;
		}	
	}

	public static void main(String[] args) {
		new Client(args[0], Integer.parseInt(args[1]));
		//KbListen.start();
		//ServerListen.start();
		//KbListen kb = new KbListen();
		//kb.start();
		//ServerListen sl = new ServerListen();
		//sl.start();
		//hb = new Heartbeat();
		//hb.start();
	}
	
	public Client(String ip, int port) {
		pattern = "private [a-z0-9_ ]*";
		try {
			serverPort = port;
			this.ip = ip;
			cliSerSoc = new ServerSocket(0);
			localPort = cliSerSoc.getLocalPort();
			contactList = new HashMap<>();
			KbListen kb = new KbListen();
			kb.start();
			ServerListen sl = new ServerListen();
			sl.start();
		}
		catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	//public class Heartbeat extends Thread {}
	
	public class ServerListen extends Thread {
		public void run(){
			while(true) {
				try {
					Socket connectSoc = cliSerSoc.accept();
					BufferedReader brS = new BufferedReader(new InputStreamReader(connectSoc.getInputStream()));
					String wholeMSG = brS.readLine();
					System.out.println(wholeMSG);
					brS.close();
					connectSoc.close();
				}
				catch (Exception e){
					System.err.println("Error: " + e.getMessage());
				}
			}
		}
	}
	public class KbListen extends Thread {
		public void run() {
			while(true)
				try {
					if(isConnect==true) {
						Scanner in2 = new Scanner(System.in);
						String line2 = in2.nextLine();
						if (!line2.matches(pattern)) {
							String response2 = null;
							//System.out.println(username);
					
							clientSoc = new Socket(ip, serverPort);
							PrintWriter pwK = new PrintWriter(clientSoc.getOutputStream());
							BufferedReader brK = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));
							pwK.println(username);
							pwK.flush();
							pwK.println(line2);
							pwK.flush();
							while ((response2 = brK.readLine())!= null){
								StringTokenizer st = new StringTokenizer(response2);
								String first = st.nextToken();
								if (first.equals("bye")) {
									System.out.println(first);
									System.exit(0);
								}
								else if (first.equals("contact")) {
									System.out.println(response2);
									String[] ctc = new String[4];
									ctc = response2.split(" ");
									contactList.put(ctc[1], new User2(ctc[1],ctc[2],Integer.parseInt(ctc[3])));
								}
								else System.out.println(response2);
							}
					
							pwK.close();
							brK.close();
							clientSoc.close();
						}
						else {
							StringTokenizer st2 = new StringTokenizer(line2);
							String cmd2 = st2.nextToken();
							String user = st2.nextToken();
							String message2 = null;
							if (st2.hasMoreTokens()) {
								message2 = st2.nextToken();
								while (st2.hasMoreTokens()) {
									message2 = message2 + st2.nextToken();
								}
							}
							privateMessage(user,message2);
						}
					}
					
					else {
						isConnect = loginServer();
						if (isConnect==true) System.out.println("Welcome log in the Server.");
					}
				}
				catch(Exception e) {
					System.out.println("Error: "+e.getMessage());
				}
				
		}
		
		public boolean loginServer() {
			Scanner in = new Scanner(System.in);
			System.out.print("Username: ");
			username = in.nextLine();
			System.out.print("Password: ");
			password = in.nextLine();
			String response = null;
			String response3 = null;
			
			try{
				Socket loginSoc = new Socket(ip, serverPort);
				PrintWriter pwL = new PrintWriter(loginSoc.getOutputStream());
				BufferedReader brL = new BufferedReader(new InputStreamReader(loginSoc.getInputStream()));
			
				String info = "login "+username+"/"+password+"/"+ip+"/"+Integer.toString(localPort);
				pwL.println(username);
				pwL.flush();
				pwL.println(info);
				pwL.flush();
				response = brL.readLine();
				//System.out.println("ha");
				while((response3=brL.readLine())!=null) {
					System.out.println(response3);
				}
				
				//System.out.println("hee");
				brL.close();
				pwL.close();
				loginSoc.close();
			
				if (response.equals("success")) {return true;}
				else if (response.equals("fail")) {
					System.out.println("Invalid username or password.");
					return false;
				}
				else if (response.equals("block")) {
					System.out.println("Blocked by the remote server.");
					return false;
				}
				else if (response.equals("duplicate")) {
					System.out.println("This username is already online.");
					return false;
				}
				else return false;
			}
			catch(Exception e){
				System.out.println("Error: "+e.getMessage());
				return false;
			}
		}
		
		public void privateMessage(String user, String message) {
			try {
				if (!contactList.containsKey(user)) {
					System.out.println("No contact information of "+user);
				}
				else {
					try{
						User2 toCon = contactList.get(user);
						Socket priSoc = new Socket(toCon.ip, toCon.port);
						PrintWriter pwP = new PrintWriter(priSoc.getOutputStream());
						pwP.println("private - " + username +" : "+message);
						pwP.flush();
						pwP.close();
						priSoc.close();
					}
					catch(Exception e) {
						System.out.println("Error: "+e.getMessage());
					}
				}
			}
			catch (Exception e) {
				System.out.println("Error: "+e.getMessage());
			}
		}
	}
}