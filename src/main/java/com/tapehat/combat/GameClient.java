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
import java.net.Socket;
import java.util.Scanner;

public class GameClient {
    String IP;
    String userName;
    String opponentUserName;
    int port;
    ActionEvent event;
    boolean switchingToBattle;

    public GameClient(String IP, int port, String userName, ActionEvent event) {
        this.IP = IP;
        this.port = port;
        this.userName = userName;
        this.event = event;
    }

    public void start() throws Exception{
        try {
            Socket socket = new Socket(IP, port);

            // Thread or listener to receive messages from the server
            new Thread(() -> {
                try {
                    ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
                    toServer.writeObject(userName);

                    ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
                    System.out.println("Connected to server.");

                    while (true) {
                        String message = (String) fromServer.readObject();
                        if (message.equals("SWITCHSCENE") && !switchingToBattle) {
                            // Trigger transition to battle_screen.fxml
                            Platform.runLater(() -> {
                                try {
                                    SwitchToBattleScene(event);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                switchingToBattle = true;
                            });
                        }
                        else{
                            opponentUserName = message;
                            System.out.println(opponentUserName);
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

    public void SwitchToBattleScene(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("battle_screen.fxml"));
        Parent root = loader.load();
        GameController gameController = loader.getController();
        gameController.start(this);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // ... Methods for sending actions, receiving game state updates...
}
