//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String username)
    {
        this(username, 1500);
    }

    private ChatClient(String username, int port)
    {
        this(username, port, "localhost");
    }

    private ChatClient(String username, int port, String server) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults
        Scanner s = new Scanner(System.in);
        ChatClient client;
        if(args.length == 1)
        {
            String user = args[0];
            client = new ChatClient(user,1500,"localhost");
        }
        else if(args.length == 2)
        {
            String user = args[0];
            int port = Integer.parseInt(args[1]);
            client = new ChatClient(user, port,"localhost");
        }
        else if(args.length == 3) {
            String user = args[0];
            int port = Integer.parseInt(args[1]);
            String server = args[2];
            client = new ChatClient(user, port, server);
        }
        else
        {
            client = new ChatClient("Unknown");
        }

        client.start();

        if(client.socket.isConnected() == true)
        {
            System.out.println("Connection accepted " + client.socket.getInetAddress());
        }

        // Send an empty message to the server
        while(true) {
            String line = s.nextLine();
            String[] commands = line.split(" ");
            int x = 0;
            if (line.equals("/logout")) {
                x = 1;
                client.sendMessage(new ChatMessage(x, line, null));
                try {
                    client.sInput.close();
                    client.sOutput.close();
                    client.socket.close();
                    break;
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else if(commands[0].equals("/msg"))
            {
                x = 2;
                line = line.substring(commands[0].length() + 1 + commands[1].length() + 1);
                client.sendMessage(new ChatMessage(x, line, commands[1]));
            }
            else if(line.equals("/list"))
            {
                x = 3;
                client.sendMessage(new ChatMessage(x, null, client.username));
            }else if(commands[0].equals("/bot")){
		x = 4;
		line = line.substring(commands[0].length() + 1);
                client.sendMessage(new ChatMessage(x,line , null));
	    }
            else {
                client.sendMessage(new ChatMessage(x, line, null));
            }
        }
    }


    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            while(true) {
                try {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
