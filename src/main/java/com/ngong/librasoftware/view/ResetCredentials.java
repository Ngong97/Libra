package com.ngong.librasoftware.view;


import com.ngong.librasoftware.DAO.DatabaseService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ResetCredentials extends Application {

    private final DatabaseService db = new DatabaseService();
    private boolean isPasswordVisible=false;

    @Override
    public void start(Stage primaryStage) {
        Image image = new Image(getClass().getResource("/images/books.png").toString()); // Create a BackgroundImage with the loaded image
        primaryStage.getIcons().add(image);
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        primaryStage.setTitle("Reset Credentials");

        // GridPane for form layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.getStyleClass().add("grid");

        // Labels and Inputs
        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-weight: bold;-fx-font-family: 'Times New Roman';-fx-font-size: 15px");
        TextField emailInput = new TextField();
        emailInput.setPromptText("One You Signed Up with");
        emailInput.setFocusTraversable(false);
        emailInput.setStyle("-fx-font-weight: bold;-fx-font-family: 'Times New Roman';-fx-font-size: 15px");

        grid.add(emailLabel, 0, 0);
        grid.add(emailInput, 1, 0);

        Label oldUsernameLabel = new Label("Old Username:");
        oldUsernameLabel.setStyle("-fx-font-weight: bold;-fx-font-family: 'Times New Roman';-fx-font-size: 15px");

        TextField oldUsernameInput = new TextField();
        oldUsernameInput.setStyle("-fx-font-weight: bold;-fx-font-family: 'Times New Roman';-fx-font-size: 15px");

        oldUsernameInput.setVisible(false);
        grid.add(oldUsernameLabel, 0, 1);
        grid.add(oldUsernameInput, 1, 1);

        Label newUsernameLabel = new Label("New Username:");
        newUsernameLabel.setStyle("-fx-font-weight: bold;-fx-font-family: 'Times New Roman';-fx-font-size: 15px");

        TextField newUsernameInput = new TextField();
        newUsernameInput.setStyle("-fx-font-weight: bold;-fx-font-family: 'Times New Roman';-fx-font-size: 15px");

        newUsernameInput.setVisible(false);
        grid.add(newUsernameLabel, 0, 2);
        grid.add(newUsernameInput, 1, 2);

        Label newPasswordLabel = new Label("New Password:");
        newPasswordLabel.setStyle("-fx-font-weight: bold;-fx-font-family: 'Times New Roman';-fx-font-size: 15px");

        PasswordField newPasswordInput = new PasswordField();

        grid.add(newPasswordLabel, 0, 3);
        grid.add(newPasswordInput, 1, 3);

        // ChoiceBox to select the reset option
        ChoiceBox<String> resetChoice = new ChoiceBox<>();
        resetChoice.getItems().addAll("Reset Password", "Reset Username", "Reset Both");
        resetChoice.setValue("Reset Password");

        Label resetOption=new Label("Reset Options");
        resetOption.setStyle("-fx-font-weight: bold;-fx-font-family: 'Lucida Calligraphy';-fx-font-size: 15px");

        grid.add(resetOption, 0, 4);
        grid.add(resetChoice, 1, 4);

        // Reset button
        Button resetButton = new Button("Reset");
        resetButton.getStyleClass().add("reset-button");
        grid.add(resetButton, 1, 5);

        // Update fields visibility based on the reset option
        resetChoice.setOnAction(event -> {
            String choice = resetChoice.getValue();
            switch (choice) {
                case "Reset Password":
                    oldUsernameInput.setVisible(false);
                    newUsernameInput.setVisible(false);
                    newPasswordInput.setVisible(true);
//                    showpassword.setVisible(true);
                    break;
                case "Reset Username":
                    oldUsernameInput.setVisible(true);
                    newUsernameInput.setVisible(true);
                    newPasswordInput.setVisible(false);
//                    showpassword.setVisible(false);
                    break;
                case "Reset Both":
                    oldUsernameInput.setVisible(true);
                    newUsernameInput.setVisible(true);
                    newPasswordInput.setVisible(true);
//                    showpassword.setVisible(true);
                    break;
            }
        });


        DropShadow sd = new DropShadow();
        sd.setOffsetY(2.0);
        sd.setColor(Color.BLACK); // Set shadow color and transparency

        resetButton.setEffect(sd);
        resetButton.setOnMouseEntered(event -> {
            resetButton.setScaleX(1.1);
            resetButton.setCursor(Cursor.HAND);
            resetButton.setEffect(sd);
        });
        resetButton.setOnMouseExited(event -> {
            resetButton.setScaleX(1.0);
        });
        resetButton.setStyle("-fx-font-family: 'Times New Roman';" +      // Set font family
                "-fx-font-size: 12pt;" +           // Set font size
                "-fx-text-fill: white;" +         // Set text color
                "-fx-font-weight: bolder");

        // Action handler for reset button
        resetButton.setOnAction(e -> {
            String email = emailInput.getText();
            String oldUsername = oldUsernameInput.getText();
            String newUsername = newUsernameInput.getText();
            String newPassword = newPasswordInput.getText();
            String choice = resetChoice.getValue();


                if (choice.equals("Reset Password")) {
                    if (!newPassword.isEmpty()){
                    if (db.resetPassword(email, newPassword)) {
                        showAlert("Password has been reset.");
                        primaryStage.close();
                    } else {
                        showAlert("No user found with the provided email.");
                    }
                }else {
                        showAlert("Please write your email and a new password!");
                }
                } else if (choice.equals("Reset Username")) {
                    if (!oldUsername.isEmpty()&&!newUsername.isEmpty()){
                    if (db.resetUsername(email, oldUsername, newUsername)) {
                        showAlert("Username has been reset.");
                        primaryStage.close();
                    } else {
                        showAlert("No matching user found with the provided email and old username.");
                    }
                }else {
                        showAlert("Please write both email, old and new username!");
                }
                } else if (choice.equals("Reset Both")) {

                    if (!oldUsername.isEmpty()&&!newUsername.isEmpty()&&!newPassword.isEmpty()){
                    if (db.resetUsernameAndPassword(email, oldUsername, newUsername, newPassword)) {
                        showAlert("Username and Password have been reset.");
                        primaryStage.close();
                    } else {
                        showAlert("No matching user found with the provided email and old username.");
                    }
                }else {
                        showAlert("Please don't leave any field empty!");
                }
            }
        });

        // Set the scene and apply CSS styling
        Scene scene = new Scene(grid, 450, 290);
        scene.getStylesheets().add("reset_style.css");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}