package com.tapehat.combat;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Scanner;

public class GameClient implements Serializable {
    String IP;
    String userName;
    String opponentUserName;
    public ObjectOutputStream toServer;
    int port;
    ActionEvent event;
    boolean switchingToBattle = false;
    boolean isPlayersTurn = false;
    GameController gameController;

    public GameClient(String IP, int port, String userName, ActionEvent event) {
        this.IP = IP;
        this.port = port;
        this.userName = userName;
        this.event = event;
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
                            isPlayersTurn = true;
                            Platform.runLater(() -> {
                                gameController.EnableButtons();
                            });
                        }
                        else if (message.startsWith("TAKE DAMAGE: ")) {
                            handleDamageMessage(message);
                        }
                        else if (message.startsWith("GAME STATE ")){
                            handleGameStateMessage(message);
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

    public void handleGameStateMessage(String message){
        String playerUserName = message.substring("GAME STATE ".length(), "GAME STATE ".length() + userName.length());
        System.out.println(playerUserName);
        if (playerUserName.equals(userName)) {
            int playerHP = Integer.parseInt(message.substring("GAME STATE ".length() + userName.length() + 1,
                    Integer.parseInt(message.substring("GAME STATE ".length() + userName.length() + 3))));
            int opponentHP = Integer.parseInt(message.substring("GAME STATE ".length() + userName.length()
                    + 4 + opponentUserName.length() + 1));
        }
        else {
            int opponentHP = Integer.parseInt(message.substring("GAME STATE ".length() + opponentUserName.length() + 1,
                    Integer.parseInt(message.substring("GAME STATE ".length() + opponentUserName.length() + 3))));
            int playerHP = Integer.parseInt(message.substring("GAME STATE ".length() + opponentUserName.length()
                    + 4 + userName.length() + 1));
        }

        
    }

    public void handleDamageMessage(String message) {
        String damageStr = message.substring("TAKE DAMAGE: ".length());

        try {
            int damage = Integer.parseInt(damageStr);

            // Valid damage!
            applyDamage(damage);

        } catch (NumberFormatException e) {
            System.err.println("Invalid damage message format: " + message);
        }
    }

    public void applyDamage(int damage) {
        gameController.player1.takeDamage(damage);
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

    // ... Methods for sending actions, receiving game state updates...
}
