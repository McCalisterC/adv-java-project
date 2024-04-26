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
    public ServerSocket server;

    public GameServer(String IP, int port) {
        this.IP = IP;
        this.port = port;
    }

    public void start() throws Exception {
        server = new ServerSocket(port);

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
                clients.get(0).startTurn();
                new Thread(() -> {
                    while(true){
                        if (clients.isEmpty()){
                            try {
                                System.out.println("Players left, server closing.");
                                server.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }).start();
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

    public int getPlayerMP(int index){
        return clients.get(index).getMP();
    }

    public int getClientIndex(String name){
        for(ClientHandler client: clients){
            if (client.username.equals(name)){
                return clients.indexOf(client);
            }
        }
        return -1;
    }

    public void stopServer() throws IOException {
        if (server != null) {
            if (!server.isClosed())
                broadcastMessage("SERVER SHUTTING DOWN");
                server.close();
                clients.clear();
        }
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
    private int playerMP;
    private boolean playerBrace;
    private boolean playAgain;

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
        playerMP = 100;
        playerBrace = false;
        playAgain = false;
    }

    public void handleMessage() throws IOException {
        new Thread(() -> {
            try {
                while(true){
                    String message = (String) ois.readObject();
                    if(message.startsWith("MP ATTACK: ")){
                        String mpStr = message.substring("MP ATTACK: ".length());
                        System.out.println(Integer.parseInt(mpStr));
                        playerMP -= Integer.parseInt(mpStr);
                        server.broadcastMessage("MP STATE " + username + ":" + server.getPlayerMP(server.getClientIndex(username)));
                    }
                    if(message.startsWith("ATTACK: ")){
                        String damageStr = message.substring("ATTACK: ".length());
                        System.out.println(Integer.parseInt(damageStr));
                        if (Integer.parseInt(damageStr) > 0){
                            if (opponent.playerBrace)
                                opponent.playerHealth -= Integer.parseInt(damageStr) / 2;
                            else
                                opponent.playerHealth -= Integer.parseInt(damageStr);
                            if (opponent.playerHealth <= 0){
                                server.broadcastMessage("GAME OVER: " + username);
                            }
                            server.broadcastMessage("GAME STATE " + username + ":" + server.getPlayerHP(server.getClientIndex(username)));
                            server.broadcastMessage("GAME STATE " + opponent.username + ":" + server.getPlayerHP(server.getClientIndex(opponent.username)));
                            server.broadcastMessage("GAME DESCRIPTION: " + username + " attacked " + opponent.username + " for " + damageStr + " damage!");
                        }
                        else if(Integer.parseInt(damageStr) < 0){
                            if (playerHealth - Integer.parseInt(damageStr) >= 100)
                                playerHealth = 100;
                            else
                                playerHealth -= Integer.parseInt(damageStr);
                            server.broadcastMessage("GAME STATE " + username + ":" + server.getPlayerHP(server.getClientIndex(username)));
                            server.broadcastMessage("GAME STATE " + opponent.username + ":" + server.getPlayerHP(server.getClientIndex(opponent.username)));
                            server.broadcastMessage("GAME DESCRIPTION: " + username + " healed for " + damageStr + " health!");
                        }
                        opponent.startTurn();
                        if (opponent.playerBrace){
                            opponent.playerBrace = false;
                        }
                    }
                    if (message.equals("BRACE")){
                        playerBrace = true;
                        if (playerMP + 10 >= 100){
                            playerMP = 100;
                        }
                        else
                            playerMP += 10;
                        server.broadcastMessage("MP STATE " + username + ":" + server.getPlayerMP(server.getClientIndex(username)));
                        server.broadcastMessage("GAME DESCRIPTION: " + username + " braces for an attack! Also restores 10 mana!");
                        opponent.startTurn();
                        if (opponent.playerBrace){
                            opponent.playerBrace = false;
                        }
                    }
                    if (message.equals("PLAY AGAIN")){
                        if (opponent.playAgain){
                            opponent.playAgain = false;
                            server.broadcastMessage("RESTART GAME");
                            playerHealth = 100;
                            playerMP = 100;
                            opponent.playerHealth = 100;
                            opponent.playerMP = 100;
                        }
                        else{
                            playAgain = true;
                            oos.writeObject("WAITING FOR OPPONENT");
                        }
                    }
                    if (message.equals("DECLINE REMATCH")){
                        opponent.oos.writeObject("REMATCH DECLINED");
                        server.stopServer();
                        Thread.currentThread().interrupt();
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
                server.broadcastMessage("GAME OVER (DISCONNECT): " + opponent.username);
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

    public int getMP(){
        return playerMP;
    }

}
