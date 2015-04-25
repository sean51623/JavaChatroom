// new PA1

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

	static ServerSocket serverSoc;
	int serverPort;
	String currentUsername;
	HashMap<String, String> account = new HashMap<>();
	HashMap<String, User> login;
	ArrayList<String> blockList = new ArrayList<String>();
	HashMap<String, HashMap<String, ArrayList<String>>> offlineMSG= new HashMap<String, HashMap<String, ArrayList<String>>>();
	HashMap<String, Integer> blockUser = new HashMap<>();
	HashMap<String, ArrayList<String>> blackList = new HashMap<String, ArrayList<String>>();
	
	public class User {
		String username;
		String ip;
		int port;
		public User (String username, String ip, int port) {
			this.username = username;
			this.ip = ip;
			this.port = port;
		}
	}

	public static void main(String[] args) {
		new Server(Integer.parseInt(args[0]));
		
	}
	
	public Server(int port){
		System.out.println("This is server, ctrl+c to shut down.");
		try {
			loadCredential(account);
			serverPort = port;
			serverSoc = new ServerSocket(serverPort);
		}
		catch (Exception e) {
			//System.err.println("Error1 ");
			System.err.println("Error: " + e.getMessage());
		}
		login = new HashMap<>();
		while(true) {
			try {
				Socket connectSoc = serverSoc.accept();
				BufferedReader br = new BufferedReader(new InputStreamReader(connectSoc.getInputStream()));
				PrintWriter pw = new PrintWriter(connectSoc.getOutputStream());
				currentUsername = br.readLine();
				//System.out.println(currentUsername);
				String wholeMessage = br.readLine();
				//System.out.println(wholeMessage);
				command(pw, wholeMessage);
				br.close();
				pw.close();
				connectSoc.close();
			}
			catch (Exception e) {
				//System.err.println("Error2 ");
				System.err.println("Error: " + e.getMessage());
			}
		}
	}
	
	public void loadCredential(HashMap<String, String> account) throws IOException{
		try{
			FileReader f1 = new FileReader("./credentials.txt");
			BufferedReader b1 = new BufferedReader(f1);
			String line = "";
			String[] temp = new String[2];
			while(b1.ready()) {
				line = b1.readLine();
				temp = line.split(" ");
				//System.out.println(temp[0]+temp[1]);
				account.put(temp[0], temp[1]);
			}
		}
		catch (Exception e) {
			//System.err.println("Error3 ");
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public void command(PrintWriter pw, String wholeMessage){
		StringTokenizer st = new StringTokenizer(wholeMessage);
		String cmd = st.nextToken();
		String ln = "";
		//System.out.println(cmd);
		if (cmd.equals("login")) {
			String userinfo = st.nextToken();
			//System.out.println(userinfo);
			int aut = userlogin(pw,userinfo);
		}
		else if (cmd.equals("logout")) {
			login.remove(currentUsername);
			pw.println("bye");
			pw.flush();
		}
		
		else if (cmd.equals("broadcast")) {
			ln = st.nextToken();
			while(st.hasMoreTokens()) {
				ln = ln + " " + st.nextToken();
			}
			broadcast(pw,ln);
		}
		
		else if (cmd.equals("message")) {
			String receiver = st.nextToken();
			ln = st.nextToken();
			while(st.hasMoreTokens()) {
				ln = ln + " " + st.nextToken();
			}
			/*if (login.containsKey(receiver))*/ personalMessage(pw, receiver, ln);
			//else offlineMessage(receiver, ln);
		}
		
		else if (cmd.equals("getaddress")) {
			String user = st.nextToken();
			getaddress(pw,user);
		}
		else if (cmd.equals("online")) {
			//pw.println("test");
			//pw.flush();
			online(pw);
		}
		else if (cmd.equals("block")) {
			String blocker = st.nextToken();
			pw.println("User " + blocker + " has been blocked.");
			pw.flush();
			if (!blackList.containsKey(blocker)) {
				ArrayList<String> bll = new ArrayList<>();
				bll.add(currentUsername);
				blackList.put(blocker,bll);
			}
			else {
				ArrayList<String> bll2 = new ArrayList<>(blackList.get(blocker));
				bll2.add(currentUsername);
				blackList.put(blocker,bll2);
			}
		}
		else if (cmd.equals("unblock")) {
			String unblocker = st.nextToken();
			pw.println("User " + unblocker + " is unblocked.");
			pw.flush();
			if (blackList.containsKey(unblocker) && blackList.get(unblocker).contains(currentUsername)) blackList.get(unblocker).remove(currentUsername);
			
		}
		else {
			pw.println("Invalid command.");
			pw.flush();
		}
	}
	
	public int userlogin(PrintWriter pw, String userinfo){
		int blockTime = 60*1000;
		String[] info = userinfo.split("/");
		
		if (account.containsKey(info[0]) && account.get(info[0]).equals(info[1]) && login.containsKey(info[0])) {
			pw.println("duplicate");
			pw.flush();
			return 2;
		}
		else if (account.containsKey(info[0]) && account.get(info[0]).equals(info[1]) && !login.containsKey(info[0])){
			blockUser.clear();
			pw.println("success");
			pw.flush();
			//pw.println("hi");
			//pw.flush();
			login.put(info[0], new User(info[0],info[2],Integer.parseInt(info[3])));
			//System.out.println(info[0]+" "+info[1]+" "+info[2]);
			//System.out.println(currentUsername);
			//System.out.println(info[0].equals(currentUsername));
			//System.out.println("test1");
			if(offlineMSG.containsKey(info[0])) {
				//System.out.println("test2");
				HashMap<String, ArrayList<String>> lol = offlineMSG.get(info[0]);
				for (String sendKey: lol.keySet()){
					ArrayList<String> sendmsg = lol.get(sendKey);
					//System.out.println("test3 " + sendmsg.size());
					for (int i = 0 ; i < sendmsg.size() ; i++) {
						//if () {
							//System.out.println(sendmsg.get(i));
							pw.println("offline message - "+sendKey+" : " + sendmsg.get(i));
							pw.flush();
						//}
					}
				}
				offlineMSG.remove(currentUsername);
			}
			
			return 1;
		}
		else if (account.containsKey(info[0]) && !account.get(info[0]).equals(info[1])){
			if (!blockUser.containsKey(info[0])) {blockUser.put(info[0],1);}
			else if (blockUser.get(info[0])<3) {
				int a = blockUser.get(info[0]) + 1;
				blockUser.put(info[0], a);
				return -3;
			}
			else {
				blockList.add(info[0]);
				return -1;
			}
			pw.println("fail");
			pw.flush();
		}
		else if (!account.containsKey(info[0])) {
			pw.println("fail");
			pw.flush();
			return -2;
		}
		return 5;
	}
	
	public void broadcast(PrintWriter pw, String message){
		for (String k: login.keySet()) {
			if (!k.equals(currentUsername) && (!blackList.containsKey(currentUsername) || !blackList.get(currentUsername).contains(k))){
				try{
					User tmp = login.get(k);
					Socket sendSoc = new Socket(tmp.ip, tmp.port);
					PrintWriter pwS = new PrintWriter(sendSoc.getOutputStream());
		
					pwS.println(currentUsername+" broadcast "+message);
					pwS.flush();
		
					pwS.close();
					sendSoc.close();
				}
				catch (Exception e){
					System.err.println("Error: " + e.getMessage());
				}
			}
			else if (!k.equals(currentUsername) && blackList.get(currentUsername).contains(k)){
				pw.println("Your message could not be delivered to some recipients");
				pw.flush();
			}
			//else continue;
		}
	}
	public void personalMessage(PrintWriter pw, String receiver, String message){
		
		//boolean test = !blackList.containsKey(receiver) || !blackList.get(receiver).contains(currentUsername);
		if(login.containsKey(receiver)){
			if (!blackList.containsKey(currentUsername) || !blackList.get(currentUsername).contains(receiver)) {
				try{
					//System.out.println(test);
					//System.out.println(!blackList.get(receiver).contains(currentUsername));
					User tmp = login.get(receiver);
					Socket sendSoc = new Socket(tmp.ip, tmp.port);
					PrintWriter pwS = new PrintWriter(sendSoc.getOutputStream());
		
					pwS.println(currentUsername+": "+message);
					pwS.flush();
		
					pwS.close();
					sendSoc.close();
				}
				catch (Exception e){
					System.err.println("Error: " + e.getMessage());
				}
			}
			else {
				pw.println("Your message could not be delivered as the recipient has blocked you");
				pw.flush();
			}
		}
		else {
			if (!blackList.containsKey(currentUsername) || !blackList.get(currentUsername).contains(receiver)) {
				System.out.println(message);
				if (!offlineMSG.containsKey(receiver)) {
					ArrayList<String> text = new ArrayList<>();
					HashMap<String, ArrayList<String>> textTemp = new HashMap<String, ArrayList<String>>();
					text.add(message);
					textTemp.put(currentUsername, text);
					offlineMSG.put(receiver, textTemp);
				}
				else {
					HashMap<String, ArrayList<String>> textTemp2 = offlineMSG.get(receiver);
					if (textTemp2.containsKey(currentUsername)){
						ArrayList<String> nextText = new ArrayList<>(textTemp2.get(currentUsername));
						nextText.add(message);
						textTemp2.put(currentUsername, nextText);
						offlineMSG.put(receiver, textTemp2);
					}
					else {
						ArrayList<String> nextText2 = new ArrayList<>();
						nextText2.add(message);
						textTemp2.put(currentUsername, nextText2);
						offlineMSG.put(receiver, textTemp2);
					}
				}
			}
			
			else {
				pw.println("You are blocked thus the offline message cannot be sent.");
				pw.flush();
			}
		}
	}
	public void online(PrintWriter pw){
		for (String k : login.keySet()) {
			//System.out.println(k+" "+currentUsername);
			//System.out.println(k.equals(currentUsername));
			if(!k.equals(currentUsername)) {
				pw.println(k);
				pw.flush();
			}
		}
	}
	public void getaddress(PrintWriter pw, String user){
		if (!login.containsKey(user)) {
			pw.println(user+" is not online.");
			pw.flush();
		}
		else {
			User tmp2 = login.get(user);
			pw.println("contact "+tmp2.username+" "+tmp2.ip+" "+tmp2.port);
			pw.flush();
		}
	}
}