//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtiils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.text.SimpleDateFormat;


final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private final String fileName;

    private ChatServer(int port, String fileName) {
        this.port = port;
        this.fileName = fileName;
    }

    /*
     * This is what starts the ChatServer.
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Banned Words File: " + fileName);
            System.out.println("Banned Words:");
            ChatFilter cf = new ChatFilter(fileName);
            cf.printBadWords();
            System.out.println();

            Date date = new Date();
            SimpleDateFormat form = new SimpleDateFormat("HH:mm:ss");
            System.out.println(form.format(date) + " " + "Server waiting for Clients on port " + port);
            while(true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        String name = args[1];

        ChatServer server = new ChatServer(port, name);
        server.start();
    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        String name;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                name = (String) sInput.readObject();
                boolean taken = false;
                for(int i = 0; i < clients.size(); i++) {
                    if(clients.get(i).username.equals(name))
                    {
                        taken = true;
                    }
                }
                if(taken == true)
                {
                    username = null;
                }
                else
                {
                    username = name;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            if(socket.isConnected() == true)
            {
                if(username == null)
                {
                    Date date = new Date();
                    SimpleDateFormat form = new SimpleDateFormat("HH:mm:ss");
                    System.out.println(form.format(date) + " " + "A client with the same username " + name + " has tried to log in!");
                    writeMessage(form.format(date) + " " + "The Username " + name + " has been already taken!\n");
                    writeMessage(form.format(date) + " " + "Manually Logging Out...");
                    remove(id);
                    return;
                }
                else {
                    Date date = new Date();
                    SimpleDateFormat form = new SimpleDateFormat("HH:mm:ss");
                    System.out.println(form.format(date) + " " + username + " just connected.");
                }
            }
            while(true) {
                try {
                    cm = (ChatMessage) sInput.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    Date date = new Date();
                    SimpleDateFormat form = new SimpleDateFormat("HH:mm:ss");
                    System.out.println(form.format(date) + " " + username + " disconnected with a LOGOUT message");
                    remove(id);
                    break;
                }
		/*
		 * each cm has the type number.
		 * 0 -> regular message broadcasted by Server.
		 * 1 -> Handle the /logout command
		 * 2 -> Send the Direct message to specific user
		 * 3 -> Show the list of the Online users.
		 * 4 -> Bot Functionality.
		 * 	- /bot command : show the all possible commands
		 */ 
                if (cm.getType() == 0) {
                    Date date = new Date();
                    SimpleDateFormat form = new SimpleDateFormat("HH:mm:ss");
                    broadcast(form.format(date) + " " + username + ": " + cm.getMessage());
                } else if (cm.getType() == 1) {
                    Date date = new Date();
                    SimpleDateFormat form = new SimpleDateFormat("HH:mm:ss");
                    System.out.println(form.format(date) + " " + username + " disconnected with a LOGOUT message");
                    remove(id);
                    break;
                } else if (cm.getType() == 2)
                {
                    Date date = new Date();
                    SimpleDateFormat form = new SimpleDateFormat("HH:mm:ss");
                    directMessage(form.format(date) + " " + username + " -> " + cm.getRecipient() + ": " + cm.getMessage(),cm.getRecipient());
                    directMessage(form.format(date) + " " + username + " -> " + cm.getRecipient() + ": " + cm.getMessage(),username);
                }
                else if (cm.getType() == 3)
                {
                    listing(cm.getRecipient());
                }else if( cm.getType() == 4){

                    SimpleDateFormat form = new SimpleDateFormat("HH:mm:ss");

                    Date date = new Date();

                    botMessage(form.format(date) + " " + username + " -> bot: : " + cm.getMessage());
		    String[] msgParse = cm.getMessage().split(" ");
		    String resultMsg="";
		    for(int j =0; j<msgParse.length;j++){
			msgParse[j] = msgParse[j].toLowerCase();
		    }
		    for(int j = 0; j<msgParse.length;j++){
			if(msgParse[j].equals("hey")){
				resultMsg +="Hey! "+username;
			}else if(msgParse[j].equals("time")){
				resultMsg +=" it is " + form.format(date);
			}else if(msgParse[j].equals("weather")){
				resultMsg += " please open the window!";
			}else if(msgParse[j].equals("command")){
				resultMsg += "Possible Commands: \n1.Hey\n2.Weather\n3.Time\n4.FortuneCookie\n";
			}else if(msgParse[j].equals("fortunecookie")){
				Random rand = new Random();
				int ranNum = rand.nextInt(6); //0~5
				switch(ranNum){
					case 0:
						resultMsg += "Think like a man of action and act like man of thought.";
						break;
					case 1:
						resultMsg += "Courage is very important. Like a muscle, it is strengthened by use.";
						break;
					case 2:
						resultMsg += "Life is the art of drawing sufficient conclusions from insufficient premises.";
						break;
					case 3:
						resultMsg += "By doubting we come at the truth.";
						break;
					case 4:
						resultMsg += "When money speaks, the truth keeps silent.";
						break;
					case 5:
						resultMsg += "Better the last smile than the first laughter.";
						break;
				}
			}else{
				resultMsg = "I don't understand. Please Enter \"command";
			}
		    }
		    
                    directMessage(form.format(date) + " bot  -> " + username + " : " + resultMsg , username);
		}
            }
        }
	//broadcast function - show message to all clients.
        private synchronized void broadcast(String message)
        {
            ChatFilter cf = new ChatFilter(fileName);
            for(int i = 0; i < clients.size(); i++)
            {
                message = cf.filter(message);
                clients.get(i).writeMessage(message + "\n");
            }
            System.out.println(message);
        }
	// print the message in Server what user enter.
	private synchronized void botMessage(String msg){
		ChatFilter cf = new ChatFilter(fileName);
		msg = cf.filter(msg);
		System.out.println(msg);
	}
	//Send the message to the specific user only.
        private synchronized void directMessage(String message, String username)
        {
            ChatFilter cf = new ChatFilter(fileName);
            for(int i = 0; i < clients.size(); i++)
            {
                if(clients.get(i).username.equals(username))
                {
                    message = cf.filter(message);
                    clients.get(i).writeMessage(message + "\n");
                }
            }
            System.out.println(message);
        }
	// list all users out. 
        private synchronized void listing(String recipient)
        {
            Date date = new Date();
            SimpleDateFormat form = new SimpleDateFormat("HH:mm:ss");
            ChatFilter cf = new ChatFilter(fileName);

            ArrayList<String> names = new ArrayList<>();
            int address = -1;
            for(int i = 0; i < clients.size(); i++)
            {
                if(recipient.equals(clients.get(i).username))
                {
                    address = i;
                }
                else {
                    names.add(clients.get(i).username);
                }
            }

            Collections.sort(names);

            clients.get(address).writeMessage(form.format(date) + " ");
            System.out.print(form.format(date) + " ");
            for(int i = 0; i < names.size(); i++)
            {
                if(i >= 1)
                {
                    clients.get(address).writeMessage("         " + cf.filter(names.get(i)) + "\n");
                    System.out.println("         " + names.get(i));
                }
                else
                {
                    clients.get(address).writeMessage(cf.filter(names.get(i)) + "\n");
                    System.out.println(names.get(i));
                }
            }
        }

	 // write the message into the outputStream.
        private boolean writeMessage(String msg)
        {
            if(socket.isConnected() == true)
            {
                try {
                    sOutput.writeObject(msg);
                    return true;
                }catch(IOException e)
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
	// remove the client from Server
        private synchronized void remove(int id)
        {
            for(int i = 0; i < clients.size(); i++)
            {
                if(clients.get(i).id == id)
                {
                    clients.get(i).close();
                    clients.remove(i);
                }
            }
        }
	// avoid to memroy leak.
        private void close()
        {
            try
            {
                sInput.close();
                sOutput.close();
                socket.close();
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
