package com.tapehat.combat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class MainMenuController {
    private Parent root;
    private Stage stage;
    private Scene scene;
    private boolean usernameValid;
    private boolean portValid;

    @FXML
    private Button createGameID;

    @FXML
    private Button joinGameID;

    @FXML
    private TextField portNum;

    @FXML
    private TextField usernameId;

    @FXML
    void onStartButton(ActionEvent event) throws Exception {
        SwitchToBattleScene(event);
    }

    @FXML
    void onQuitButton(ActionEvent event) {
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    public void initialize(){
        portValidation();
        usernameValidation();
    }

    public void onCreateGame(ActionEvent event) throws Exception {
        Thread serverManager = new Thread(new ServerManager("localhost", Integer.parseInt(portNum.getText()), usernameId.getText()));
        serverManager.start();
        GameClient player = new GameClient("localhost", Integer.parseInt(portNum.getText()), usernameId.getText());
        player.event = event;
        player.start();
    }

    public void onJoinGame(ActionEvent event) throws Exception {
        GameClient player = new GameClient("localhost", Integer.parseInt(portNum.getText()), usernameId.getText());
        player.event = event;
        player.start();
    }

    public void usernameValidation(){
        if(usernameId != null){
            usernameId.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 3){
                    usernameValid = true;
                    if(portValid){
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
                    if(newValue.length() > 0){
                        // Check for valid port range
                        int port = Integer.parseInt(newValue);
                        if (port >= 0 && port <= 65535) {
                            portValid = true;
                            if (usernameValid)
                                if(createGameID != null)
                                    createGameID.setDisable(false); // Valid port, enable button
                                else
                                    joinGameID.setDisable(false);
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

    public void SwitchToBattleScene(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("battle_screen.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
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

    public ServerManager(String IP, int port, String userName) {
        this.IP = IP;
        this.port = port;
        this.userName = userName;
    }

    @Override
    public void run(){
        try {
            GameServer server = new GameServer(IP, port);
            server.start();
        }
        catch (Exception ex){
            System.out.println("Error creating server: " + ex);
        }
    }
}
