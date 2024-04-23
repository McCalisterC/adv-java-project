package com.tapehat.combat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameServer {
    String IP;
    int port;
    boolean hasSwitchedToBattle;
    private List<ClientHandler> clients = new ArrayList<>();

    public GameServer(String IP, int port) {
        this.IP = IP;
        this.port = port;
    }

    public void start() throws Exception {
        ServerSocket server = new ServerSocket(port);


        int clientCount = 0;
        while (true) {
            if(clientCount == 2 && !hasSwitchedToBattle){
                // Start countdown
                hasSwitchedToBattle = true;
                broadcastMessage("SWITCHSCENE"); // Broadcast to all clients
                for (ClientHandler client: clients){
                    client.sendOpponentUsernames(clients);
                }
            }
            else{
                System.out.println("Waiting for client");
                Socket socket = server.accept();


                ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());

                String username = (String) fromClient.readObject();

                clients.add(new ClientHandler(socket, username)); // Add the new client's socket to the list
                clientCount++;
            }
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler clientSocket : clients) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.socket.getOutputStream());
                out.writeObject(message);
                out.flush(); // Ensure the message is sent
            } catch (IOException e) {
                System.err.println("Error broadcasting to client: " + e.getMessage());
                // Consider removing clients that have connection issues
            }
        }
    }

}


class ClientHandler{
    public Socket socket;
    public String username;
    public ObjectOutputStream oos;

    public ClientHandler(Socket socket, String username) throws Exception {
        this.socket = socket;
        this.username = username;
        this.oos = new ObjectOutputStream(socket.getOutputStream());
    }

    public void sendOpponentUsernames(List<ClientHandler> clients) throws Exception {
        for (ClientHandler client : clients){
            if(client != this){
                client.oos.writeObject(username);
                client.oos.flush();
            }
        }
    }
}
