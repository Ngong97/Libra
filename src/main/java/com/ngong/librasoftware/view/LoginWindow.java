package com.ngong.librasoftware.view;

import com.ngong.librasoftware.Controller.DashboardApp;
import com.ngong.librasoftware.DAO.DatabaseService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginWindow extends Application {
    private final DatabaseService db = new DatabaseService();

    private boolean isPasswordVisible=false;

    @Override
    public void start(Stage primaryStage) {

        Image icon = new Image(getClass().getResource("/images/books.png").toString()); // Create a BackgroundImage with the loaded image

        primaryStage.getIcons().add(icon);

        Label titleLabel = new Label("Sign in to access Dashboard");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setTextFill(Color.web("#333333"));

        // Username and Password fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(280);
        usernameField.setStyle("-fx-font-size: 15");
        PasswordField passwordField = new PasswordField();
        passwordField.setStyle("-fx-font-size: 15");
        passwordField.setMaxWidth(280);
        passwordField.setPromptText("Password");

        // Forgot Password link
        Hyperlink forgotPasswordLink = new Hyperlink("Forgot your password?");
        forgotPasswordLink.setTextFill(Color.web("#1a73e8"));
        forgotPasswordLink.setStyle("-fx-font-size: 12px;");
        forgotPasswordLink.setOnAction(event -> {
           showResetCredentials();
        });
        // Login button
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(200);
        loginButton.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-weight: bold;");


        DropShadow sd = new DropShadow();
        sd.setOffsetY(2.0);
        sd.setColor(Color.BLACK); // Set shadow color and transparency

        loginButton.setEffect(sd);
        loginButton.setOnMouseEntered(event -> {
            loginButton.setScaleX(1.1);
            loginButton.setCursor(Cursor.HAND);
            loginButton.setEffect(sd);
        });
        loginButton.setOnMouseExited(event -> {
            loginButton.setScaleX(1.0);
        });
        loginButton.setStyle("-fx-font-family: 'Times New Roman';" +      // Set font family
                "-fx-font-size: 12pt;" +           // Set font size
                "-fx-text-fill: white;" +         // Set text color
                "-fx-background-color: #5876e3;-fx-font-weight: bolder");


        TextField passtextfield=new TextField();
        passtextfield.setPromptText("Enter your password");
        passtextfield.setStyle("-fx-font-family: 'Times New Roman';-fx-font-size: 16;-fx-font-weight: bold");
        passtextfield.setVisible(false);

        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (!username.isBlank()&&!password.isBlank()) {
                if (db.checkCredentials(username, password)) {
                    usernameField.setEditable(false);
                    passwordField.setEditable(false);
                    usernameField.setDisable(true);
                    passwordField.setDisable(true);
                    loginButton.setDisable(true);
                    showLoadingPopUp(primaryStage);
                }else {
//                    snackbar.show("Check Credentials!");
                }
            }
            else {
//                snackbar.show("Fill all!");

            }

        });

//        Hyperlink replaceDbLink = new Hyperlink("Replace Database");
//        replaceDbLink.setOnAction(e ->{
//            primaryStage.close();
//            new DatabaseFileSwitcherWindow().show(primaryStage);
//        });


        VBox formLayout = new VBox(20, titleLabel, usernameField,passwordField, forgotPasswordLink, loginButton);
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setPadding(new Insets(20));
        formLayout.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #dddddd; -fx-border-width: 1px;");
        formLayout.setPrefWidth(350);

        // Main scene
        Scene scene = new Scene(formLayout, 550, 580);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login Form");
        primaryStage.show();
    }

    private void showResetCredentials() {
        new PasswordRecoveryForm().launch();
    }


    private void showLoadingPopUp(Stage primarystage) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.TRANSPARENT);
        ProgressIndicator progressIndicator=new ProgressIndicator();
        progressIndicator.setMinSize(75,75);
        Label label=new Label("Logging in...");
        label.setStyle("-fx-text-fill: #020202;-fx-font-weight: bolder;-fx-font-size: 20;-fx-font-family: Consolas");

        VBox layout=new VBox(10,progressIndicator,label);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 20;-fx-background-color: rgb(0,0,0,0.1);-fx-border-radius: 10;");

        Scene scene=new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.setWidth(300);
        popup.setHeight(200);
        popup.show();


        new Thread(() ->{
            try {
                Thread.sleep(3000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() ->{
                popup.close();
                primarystage.close();
//                stage.close();
                new DashboardApp().start(new Stage());
            });
        }).start();

    }


    // Main method to launch the application
    public static void main(String[] args) {
        launch(args);
    }
}
