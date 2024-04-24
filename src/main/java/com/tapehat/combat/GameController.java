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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class GameController implements Serializable {
    private Parent root;
    private Stage stage;
    private Scene scene;

    @FXML
    private Pane battleScreen; // Connect to the battle screen in your FXML

    @FXML
    private Label player1HpLabel;

    @FXML
    private Label player2HpLabel;

    private List<Button> buttons = new ArrayList<Button>();

    @FXML
    private Button attackButton;

    @FXML
    private Button healButton;

    @FXML
    private Label winText;

    @FXML
    private Label loseText;

    @FXML
    private Button playAgainButton;

    @FXML
    private Button returnToMenuButton;

    @FXML
    private Pane endGamePane;

    // ... other GUI elements

    public Character player1;
    public Character player2;
    private GameClient gameClient; // Assuming you have your GameClient handling connection

    @FXML
    public void initialize() {
    }

    public void start(GameClient gameClient){
        buttons.add(attackButton);
        buttons.add(healButton);

        DisableButtons();

        this.gameClient = gameClient;

        player1 = new Character(gameClient.userName, 100);
        player2 = new Character(gameClient.opponentUserName, 100);

        // Initialize HP labels
        player1HpLabel.setText(player1.getName() + " HP: " + player1.getHp());
        player2HpLabel.setText(player2.getName() + " HP: " + player2.getHp());
    }

    @FXML
    void onAttack(ActionEvent event) {
        try {
            gameClient.toServer.writeObject("ATTACK: 50");
            gameClient.toServer.flush();
            DisableButtons();
        } catch (Exception e) {
            // Handle the exception
        }
    }

    @FXML
    void onHeal(ActionEvent event) {
        try {
            gameClient.toServer.writeObject("ATTACK: -50");
            gameClient.toServer.flush();
            DisableButtons();
        } catch (Exception e) {
            // Handle the exception
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

    public void DisableButtons(){
        for(Button b : buttons){
            b.setDisable(true);
        }
    }

    public void EnableButtons(){
        for(Button b : buttons){
            b.setDisable(false);
        }
    }

    public void SetPlayerHP(){
        player1HpLabel.setText(player1.getName() + " HP: " + player1.getHp());
        player2HpLabel.setText(player2.getName() + " HP: " + player2.getHp());
    }

    public void EndGame(boolean won){
        DisableButtons();
        endGamePane.setDisable(false);
        endGamePane.setVisible(true);
        if (won){
            winText.setOpacity(1);
        }
        else{
            loseText.setOpacity(1);
        }
        playAgainButton.setOpacity(1);
        playAgainButton.setDisable(false);
        returnToMenuButton.setOpacity(1);
        returnToMenuButton.setDisable(false);
    }
}
