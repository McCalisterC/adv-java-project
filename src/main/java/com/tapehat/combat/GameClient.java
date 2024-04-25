package com.tapehat.combat;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class GameClient implements Serializable {
    String IP;
    String userName;
    String opponentUserName;
    public transient ObjectOutputStream toServer;
    int port;
    transient ActionEvent event;
    boolean switchingToBattle = false;
    boolean isPlayersTurn = false;
    boolean gameOver = false;
    GameController gameController;

    public GameClient(String IP, int port, String userName) {
        this.IP = IP;
        this.port = port;
        this.userName = userName;
    }

    public void start() throws Exception{
        try {
            Socket socket = new Socket(IP, port);

            toServer = new ObjectOutputStream(socket.getOutputStream());
            toServer.writeObject(userName);

            toServer.writeObject(this);

            ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connected to server.");

            // Thread or listener to receive messages from the server
            new Thread(() -> {
                try {
                    while (true) {
                        System.out.println("Waiting for user...");
                        String message = (String) fromServer.readObject();
                        System.out.println("Received message: " + message);
                        if (message.equals("SWITCHSCENE") && !switchingToBattle) {
                            // Trigger transition to battle_screen.fxml
                            Platform.runLater(() -> {
                                try {
                                    System.out.println("Switching to Battle");
                                    SwitchToBattleScene(event);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                switchingToBattle = true;
                            });
                        }
                        else if(message.equals("START TURN")){
                            if(!gameOver){
                                isPlayersTurn = true;
                                Platform.runLater(() -> {
                                    gameController.EnableButtons();
                                    gameController.CheckMPButtons();
                                });
                            }
                        }
                        else if (message.startsWith("GAME STATE ")){
                            handleGameStateMessage(message);
                        }
                        else if (message.startsWith("MP STATE ")){
                            handleMPStateMessage(message);
                        }
                        else if (message.startsWith("GAME OVER: ")){
                            System.out.println(message.substring("GAME OVER: ".length()));
                            if (message.substring("GAME OVER: ".length()).equals(userName)){
                                handleEndGameMessage(true);
                            }
                            else {
                                handleEndGameMessage(false);
                            }
                        }
                        else if (message.equals("SERVER SHUTTING DOWN")){
                            closeConnection(socket);
                        }
                        else{
                            opponentUserName = message;
                        }
                    }
                } catch (Exception ex) {
                    // Handle exceptions
                }
            }).start();
        } catch (Exception ex) {
            System.out.println("Error connecting to server:" + ex);
        }
    }

    public void handleEndGameMessage(boolean won){
        gameOver = true;
        gameController.EndGame(won);
    }

    public void handleGameStateMessage(String message){
        String playerUserName = message.substring("GAME STATE ".length(), "GAME STATE ".length() + userName.length());
        int playerHP;
        System.out.println(playerUserName);
        if (playerUserName.equals(userName)) {
            System.out.println("playerUserName is the same");
            playerHP = Integer.parseInt(message.substring("GAME STATE ".length() + userName.length() + 1));
            gameController.player1.setHp(playerHP);
        }
        else {
            System.out.println("playerUserName is not the same");
            playerHP = Integer.parseInt(message.substring("GAME STATE ".length() + opponentUserName.length() + 1));
            gameController.player2.setHp(playerHP);
        }
        System.out.println("Player : " + playerHP + "HP");
        Platform.runLater(() ->
                gameController.SetPlayerHP());
    }

    public void handleMPStateMessage(String message){
        String playerUserName = message.substring("MP STATE ".length(), "MP STATE ".length() + userName.length());
        int playerMP;
        System.out.println(playerUserName);
        if (playerUserName.equals(userName)) {
            System.out.println("playerUserName is the same");
            playerMP = Integer.parseInt(message.substring("MP STATE ".length() + userName.length() + 1));
            gameController.player1.setMp(playerMP);
        }
        else {
            System.out.println("playerUserName is not the same");
            playerMP = Integer.parseInt(message.substring("MP STATE ".length() + opponentUserName.length() + 1));
            gameController.player2.setMp(playerMP);
        }
        System.out.println("Player : " + playerMP + "MP");
        Platform.runLater(() ->
                gameController.SetPlayerMP());
    }

    public void SwitchToBattleScene(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("battle_screen.fxml"));
        Parent root = loader.load();
        gameController = loader.getController();
        gameController.start(this);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void closeConnection(Socket socket) throws Exception {
        socket.close();
    }
}
