package com.ngong.librasoftware.view;

import com.ngong.librasoftware.Controller.DashboardApp;
import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.utils.UIUtils;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class LoginWindow extends Application {
    private final DatabaseService db = new DatabaseService();

    private int failedAttempts = 0;

    private final Label cooldownLabel = new Label();
    private Timeline cooldownTimeline;

    @Override
    public void start(Stage primaryStage) {
        UIUtils.applyAppIcon(primaryStage);
        Label titleLabel = new Label("Sign in to access Dashboard");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setTextFill(Color.web("#333333"));

        // Username and Password fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(280);
        usernameField.setStyle("-fx-font-size: 15");
        usernameField.getStyleClass().add("recovery-input");
        PasswordField passwordField = new PasswordField();
        passwordField.setStyle("-fx-font-size: 15");
        passwordField.setMaxWidth(280);
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("recovery-input");

        Hyperlink forgotPasswordLink = new Hyperlink("Forgot password?");
        forgotPasswordLink.setVisible(false); // initially hidden
        forgotPasswordLink.setStyle("-fx-text-fill: #1976D2; -fx-underline: true;");
        forgotPasswordLink.setOnAction(e -> new PasswordRecoveryForm().launch());


        // Login button
        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.setPrefWidth(200);
        loginButton.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-weight: bold;");

        ScaleTransition pressLogin = new ScaleTransition(Duration.millis(80), loginButton);
        pressLogin.setToX(0.95);
        pressLogin.setToY(0.95);

        ScaleTransition releaseLogin = new ScaleTransition(Duration.millis(80), loginButton);
        releaseLogin.setToX(1.0);
        releaseLogin.setToY(1.0);

        loginButton.setOnMousePressed(e -> pressLogin.play());
        loginButton.setOnMouseReleased(e -> releaseLogin.play());


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



        VBox formLayout = new VBox(20, titleLabel, usernameField,passwordField, forgotPasswordLink, loginButton);
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setPadding(new Insets(20));
        formLayout.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #dddddd; -fx-border-width: 1px;");
        formLayout.setPrefWidth(350);



        loginButton.setOnAction(e -> {
            String enteredUsername = usernameField.getText();
            String enteredPassword = passwordField.getText();

            boolean isValid = db.checkCredentials(enteredUsername, enteredPassword);

            if (isValid) {
                showLoadingPopUp(primaryStage);
                failedAttempts = 0;
            } else {
                if (failedAttempts < 5) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information");
                    alert.setHeaderText(null);
                    alert.setContentText("Incorrect credentials. Attempt " + failedAttempts + " of 5.");
                    alert.showAndWait();
                    failedAttempts+=1;
                    if (failedAttempts >= 2) {
                        forgotPasswordLink.setVisible(true);
                        forgotPasswordLink.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
                    }
                } else {
                    loginButton.setDisable(true);
                    forgotPasswordLink.setVisible(true);
                    forgotPasswordLink.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");

                    cooldownLabel.setText("Please wait 15 seconds...");
                    cooldownLabel.setStyle("-fx-text-fill: #1976D2; -fx-font-weight: bold;");

                    // Add cooldownLabel to formLayout
                    formLayout.getChildren().add(cooldownLabel);

                    cooldownTimeline = new Timeline();
                    for (int i = 1; i <= 15; i++) {
                        final int secondsLeft = 15 - i;
                        cooldownTimeline.getKeyFrames().add(
                                new KeyFrame(Duration.seconds(i), ev ->
                                        cooldownLabel.setText("Please wait " + secondsLeft + " seconds...")
                                )
                        );
                    }

                    cooldownTimeline.setOnFinished(ev -> {
                        formLayout.getChildren().remove(cooldownLabel);
                        loginButton.setDisable(false);
                        failedAttempts = 0;
                    });

                    cooldownTimeline.play();
                }

            }
        });





        // Main scene
        Scene scene = new Scene(formLayout, 550, 580);
        scene.getStylesheets().add(getClass().getResource("/recovery.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Login Form");
        primaryStage.show();
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
