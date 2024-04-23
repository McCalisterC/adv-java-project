package com.tapehat.combat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class GameController {
    private Parent root;
    private Stage stage;
    private Scene scene;

    @FXML
    private Pane battleScreen; // Connect to the battle screen in your FXML

    @FXML
    private Label player1HpLabel;

    @FXML
    private Label player2HpLabel;

    @FXML
    private Button attackButton;

    @FXML
    private Button healButton;

    // ... other GUI elements

    private Character player1;
    private Character player2;
    private GameClient gameClient; // Assuming you have your GameClient handling connection

    @FXML
    public void initialize() {

    }

    public void start(GameClient gameClient){
        this.gameClient = gameClient;

        player1 = new Character(gameClient.userName, 100);
        player2 = new Character(gameClient.opponentUserName, 100);

        // Initialize HP labels
        player1HpLabel.setText(player1.getName() + " HP: " + player1.getHp());
        player2HpLabel.setText(player2.getName() + " HP: " + player2.getHp());
    }

    @FXML
    void onAttack(ActionEvent event) {
        // Placeholder:
        player2.takeDamage(10);
        player2HpLabel.setText("Player 2 HP: " + player2.getHp());
        // TODO: Send 'attack' action to the GameServer
    }

    @FXML
    void onHeal(ActionEvent event) {
        // Placeholder:
        player1.heal();
        player1HpLabel.setText("Player 1 HP: " + player1.getHp());
        // TODO: Send 'heal' action to the GameServer
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
}