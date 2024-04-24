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
    private int currentTurn = 0;

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
                   client.getOpponent(clients);
                }
                clients.get(currentTurn).startTurn();
            }
            else{
                System.out.println("Waiting for client");
                Socket socket = server.accept();


                ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());

                String username = (String) fromClient.readObject();
                GameClient playerClient = (GameClient) fromClient.readObject();

                clients.add(new ClientHandler(socket, username, toClient, fromClient, this, playerClient)); // Add the new client's socket to the list
                clientCount++;
            }
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler clientSocket : clients) {
            try {
                System.out.println("Sending message: " + message);
                clientSocket.oos.writeObject(message);
                clientSocket.oos.flush(); // Ensure the message is sent
            } catch (IOException e) {
                System.err.println("Error broadcasting to client: " + e.getMessage());
                // Consider removing clients that have connection issues
            }
        }
    }

    public int getPlayerHP(int index){
        return clients.get(index).getHealth();
    }

    public int getClientIndex(String name){
        for(ClientHandler client: clients){
            if (client.username.equals(name)){
                return clients.indexOf(client);
            }
        }
        return -1;
    }

}


class ClientHandler {
    public Socket socket;
    public String username;
    public ObjectOutputStream oos;
    public ObjectInputStream ois;
    public GameServer server;
    public GameClient client;
    private ClientHandler opponent;
    private int playerHealth;

    public ClientHandler(Socket socket, String username, ObjectOutputStream oos, ObjectInputStream ois,
                         GameServer server, GameClient client) throws Exception {
        this.socket = socket;
        this.username = username;
        this.oos = oos;
        this.ois = ois;
        this.server = server;
        this.client = client;
        handleMessage();
        playerHealth = 100;
    }

    public void handleMessage() throws IOException {
        new Thread(() -> {
            try {
                while(true){
                    String message = (String) ois.readObject();
                    if(message.startsWith("ATTACK: ")){
                        String damageStr = message.substring("ATTACK: ".length());
                        opponent.oos.writeObject("TAKE DAMAGE: " + damageStr);
                        opponent.playerHealth -= Integer.parseInt(damageStr);
                        server.broadcastMessage("GAME STATE " + username + ":" + server.getPlayerHP(server.getClientIndex(username)));
                        server.broadcastMessage("GAME STATE " + opponent.username + ":" + server.getPlayerHP(server.getClientIndex(opponent.username)));
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    public void startTurn() throws IOException {
        oos.writeObject("START TURN");
    }

    public void sendOpponentUsernames(List<ClientHandler> clients) throws Exception {
        for (ClientHandler client : clients){
            if(client != this){
                client.oos.writeObject(username);
                client.oos.flush();
            }
        }
    }

    public void getOpponent(List<ClientHandler> server) {
        for (ClientHandler client : server) {
            if (client != this) {
                opponent = client;
            }
        }
    }

    public int getHealth(){
        return playerHealth;
    }

}
