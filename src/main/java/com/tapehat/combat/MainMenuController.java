package com.tapehat.combat;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;


public class MainMenuController {
    private Parent root;
    private Stage stage;
    private Scene scene;
    private boolean usernameValid;
    private boolean portValid;
    private boolean ipValid;
    private ServerManager serverManager;
    private GameClient player;

    @FXML
    private Button createGameID;

    @FXML
    private Button joinGameID;

    @FXML
    private Button cancelId;

    @FXML
    private TextField portNum;

    @FXML
    private TextField usernameId;

    @FXML
    public TextField ipText;

    @FXML
    public Text feedbackId;

    @FXML
    void onQuitButton(ActionEvent event) {
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.close();
        Platform.exit();
        System.exit(0);
    }

    @FXML
    public void initialize() throws URISyntaxException {
        portValidation();
        usernameValidation();
        ipValidation();
    }

    public void onCreateGame(ActionEvent event) throws Exception {
        serverManager = new ServerManager("localhost", Integer.parseInt(portNum.getText()), usernameId.getText());
        Thread thread = new Thread(serverManager);
        thread.start();
        player = new GameClient("localhost", Integer.parseInt(portNum.getText()), usernameId.getText());
        player.event = event;
        player.start(feedbackId);
        disableUICreate();
        System.out.println(serverManager.IP + " " + serverManager.port);
        System.out.println(player.IP + " " + player.port);
        feedbackId.setText("Created Game, IP for Opponent is: " + InetAddress.getLocalHost().getHostAddress());
    }

    public void onJoinGame(ActionEvent event) {
        try {
            GameClient player = new GameClient(ipText.getText(), Integer.parseInt(portNum.getText()), usernameId.getText());
            player.event = event;
            player.start(feedbackId);
        }
        catch (Exception e) {
            feedbackId.setText("Could not connect to server, try again!");
        }
    }

    public void disableUICreate(){
        createGameID.setDisable(true);
        portNum.setDisable(true);
        usernameId.setDisable(true);
    }

    public void enableUICreate(){
        createGameID.setDisable(false);
        portNum.setDisable(false);
        usernameId.setDisable(false);
    }

    public void usernameValidation(){
        if(usernameId != null){
            usernameId.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 3){
                    usernameValid = true;
                    if (joinGameID != null){
                        if(portValid && ipValid){
                            if(createGameID != null)
                                createGameID.setDisable(false); // Valid port, enable button
                            else
                                joinGameID.setDisable(false);
                        }
                    }
                    else {
                        if(portValid){
                            if(createGameID != null)
                                createGameID.setDisable(false); // Valid port, enable button
                            else
                                joinGameID.setDisable(false);
                        }
                    }
                }
                else{
                    if(createGameID != null)
                        createGameID.setDisable(true); // Valid port, enable button
                    else
                        joinGameID.setDisable(true);
                    usernameValid = false;
                }
            });
        }
    }

    public void portValidation() {
        if(portNum != null){
            portNum.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) { // Only allow digits
                    portNum.setText(oldValue); // Revert to old value if invalid
                } else {
                    if(!newValue.isEmpty()){
                        // Check for valid port range
                        int port = Integer.parseInt(newValue);
                        if (port >= 0 && port <= 65535) {
                            portValid = true;
                            if (joinGameID != null){
                                if (usernameValid && ipValid)
                                    if(createGameID != null)
                                        createGameID.setDisable(false); // Valid port, enable button
                                    else
                                        joinGameID.setDisable(false);
                            }
                            else {
                                if (usernameValid)
                                    if(createGameID != null)
                                        createGameID.setDisable(false); // Valid port, enable button
                                    else
                                        joinGameID.setDisable(false);
                            }

                        } else {
                            if(port >= 65535){
                                portNum.setText(oldValue);
                            }
                            else
                                portNum.setText(oldValue);
                        }
                    }
                    else if(newValue == ""){
                        portValid = false;
                        if(createGameID != null)
                            createGameID.setDisable(true); // Valid port, enable button
                        else
                            joinGameID.setDisable(true);
                    }
                }
            });
        }
    }

    public void ipValidation(){
        if(ipText != null){
            ipText.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty()){
                    ipValid = true;
                    if(portValid && usernameValid){
                        if(createGameID != null)
                            createGameID.setDisable(false); // Valid port, enable button
                        else
                            joinGameID.setDisable(false);
                    }
                }
                else{
                    if(createGameID != null)
                        createGameID.setDisable(true); // Valid port, enable button
                    else
                        joinGameID.setDisable(true);
                    ipValid = false;
                }
            });
        }
    }

    public void cancelButton(ActionEvent event) throws Exception {
        if (serverManager != null){
            if(createGameID.isDisabled()){
                serverManager.stopServer();
                enableUICreate();
            }
        }
        else {
            SwitchToMainMenuScene(event);
        }
    }

    public void SwitchToMainMenuScene(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main_menu_screen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void SwitchToCreateGameScene(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("create_game_screen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void SwitchToJoinGameScene(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("join_game_screen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}

class ServerManager implements Runnable {
    String IP;
    String userName;
    int port;
    GameServer server;

    public ServerManager(String IP, int port, String userName) {
        this.IP = IP;
        this.port = port;
        this.userName = userName;
    }

    @Override
    public void run(){
        try {
            server = new GameServer(IP, port);
            server.start();
        }
        catch (Exception ex){
            System.out.println("Error creating server: " + ex);
        }
    }

    public void stopServer() throws IOException {
        server.stopServer();
        Thread.currentThread().interrupt();
    }
}
