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
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class GameController implements Serializable {
    private Parent root;
    private Stage stage;
    private Scene scene;
    public boolean won;
    public boolean serverAvailable;

    @FXML
    private Pane battleScreen; // Connect to the battle screen in your FXML

    @FXML
    private Label player1HpLabel;

    @FXML
    private Label player1MpLabel;

    @FXML
    private Label player2HpLabel;

    @FXML
    private Label player2MpLabel;

    private List<Button> buttons = new ArrayList<Button>();

    @FXML
    private Button attackButton;

    @FXML
    private Button healButton;

    @FXML
    private Button braceButton;

    @FXML
    private Button mpAttackButton;

    @FXML
    private Label winText;

    @FXML
    private Label loseText;

    @FXML
    private Button playAgainButton;

    @FXML
    private Button returnToMenuButton;

    @FXML
    private Label endGameFeedback;

    @FXML
    private Pane endGamePane;

    // ... other GUI elements

    public Character player1;
    public Character player2;
    private GameClient gameClient; // Assuming you have your GameClient handling connection

    public void start(GameClient gameClient){
        buttons.add(attackButton);
        buttons.add(healButton);
        buttons.add(braceButton);
        buttons.add(mpAttackButton);

        DisableButtons();

        this.gameClient = gameClient;
        serverAvailable = true;

        player1 = new Character(gameClient.userName, 100, 100);
        player2 = new Character(gameClient.opponentUserName, 100, 100);

        // Initialize HP labels
        player1HpLabel.setText(player1.getName() + " HP: " + player1.getHp());
        player2HpLabel.setText(player2.getName() + " HP: " + player2.getHp());
    }

    @FXML
    void onAttack(ActionEvent event) {
        try {
            gameClient.toServer.writeObject("ATTACK: 10");
            gameClient.toServer.flush();
            DisableButtons();
        } catch (Exception e) {
            // Handle the exception
        }
    }

    @FXML
    void onMPAttack(ActionEvent event) {
        try {
            gameClient.toServer.writeObject("MP ATTACK: 50");
            gameClient.toServer.flush();
            gameClient.toServer.writeObject("ATTACK: 100");
            gameClient.toServer.flush();
            DisableButtons();
        } catch (Exception e) {
            // Handle the exception
        }
    }

    @FXML
    void onHeal(ActionEvent event) {
        try {
            gameClient.toServer.writeObject("MP ATTACK: 50");
            gameClient.toServer.flush();
            gameClient.toServer.writeObject("ATTACK: -50");
            gameClient.toServer.flush();
            DisableButtons();
        } catch (Exception e) {
            // Handle the exception
        }
    }

    @FXML
    void onBrace(ActionEvent event) {
        try {
            gameClient.toServer.writeObject("BRACE");
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

    public void SetPlayerMP(){
        player1MpLabel.setText(player1.getName() + " MP: " + player1.getMp());
        player2MpLabel.setText(player2.getName() + " MP: " + player2.getMp());
    }

    public void EndGame(boolean endGame, boolean disconnect){
        DisableButtons();
        endGamePane.setDisable(false);
        endGamePane.setVisible(true);
        if (endGame){
            winText.setOpacity(1);
            won = true;
        }
        else{
            loseText.setOpacity(1);
            won = false;
        }
        playAgainButton.setOpacity(1);
        if (disconnect){
            playAgainButton.setDisable(true);
            Platform.runLater(() -> {
                endGameFeedback.setOpacity(1);
                endGameFeedback.setText("Opponent Disconnected");
            });
        }
        else {
            playAgainButton.setDisable(false);
        }
        returnToMenuButton.setOpacity(1);
        returnToMenuButton.setDisable(false);
    }

    public void onPlayAgain(ActionEvent event) throws IOException {
        gameClient.toServer.writeObject("PLAY AGAIN");
        playAgainButton.setDisable(true);
    }

    public void Restart() throws IOException {
        endGamePane.setDisable(true);
        endGamePane.setVisible(false);
        winText.setOpacity(0);
        loseText.setOpacity(0);
        playAgainButton.setOpacity(0);
        endGameFeedback.setOpacity(0);
        playAgainButton.setDisable(true);
        returnToMenuButton.setOpacity(0);
        returnToMenuButton.setDisable(true);
    }

    public void CheckMPButtons(){
        if(player1.getMp() < 50){
            healButton.setDisable(true);
            mpAttackButton.setDisable(true);
        }
    }

    public void WaitingForOpponentMessage(){
        endGameFeedback.setOpacity(1);
        endGameFeedback.setText("Waiting for opponent...");
    }

    public void onRematchDeclined(){
        playAgainButton.setDisable(true);
        endGameFeedback.setOpacity(1);
        endGameFeedback.setText("Rematch declined...");
    }

    public void onDeclineRematch(ActionEvent event) throws Exception {
        if (serverAvailable)
            gameClient.toServer.writeObject("DECLINE REMATCH");
        SwitchToMainMenuScene(event);
        gameClient.closeConnection(gameClient.socket);
    }

    public void updateDescriptionText(String text){
        //descriptionText.setText(text);
    }
}
