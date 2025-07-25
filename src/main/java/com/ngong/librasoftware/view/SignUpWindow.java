package com.ngong.librasoftware.view;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.utils.UIUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SignUpWindow extends Application {

    // Database connection parameters
    private final DatabaseService db = new DatabaseService();

    //private static final String DB_URL = "jdbc:sqlite:data/librarydatabase.db";//this is the embedded path
    private boolean isPasswordVisible=false;


    @Override
    public void start(Stage primaryStage) {
        UIUtils.applyAppIcon(primaryStage);
        primaryStage.setTitle("Sign Up Form"); // Labels
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-font-weight: bold;-fx-font-family: Consolas;-fx-font-size: 18");
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-weight: bold;-fx-font-family: Consolas;-fx-font-size: 18");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setMaxWidth(250);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefWidth(300);



        Label schoolNameLabel = new Label("School Name:"); // Input Fields
        schoolNameLabel.setStyle("-fx-font-weight: bold;-fx-font-family: Consolas;-fx-font-size: 18");

        TextField schoolNameField = new TextField();
        schoolNameField.setPromptText("Write your school name");
        schoolNameField.setMaxWidth(250);



        Label titleLabel = new Label("Create user account");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setTextFill(Color.web("#333333"));

        DropShadow sd = new DropShadow();
        sd.setOffsetY(2.0);
        sd.setColor(Color.BLACK); // Set shadow color and transparency
// Sign Up Button
        Button signUpButton = new Button("Submit");
        signUpButton.setDefaultButton(true);
        signUpButton.setEffect(sd);
        signUpButton.setOnMouseEntered(event -> {
            signUpButton.setScaleX(1.1);
            signUpButton.setCursor(Cursor.HAND);
            signUpButton.setEffect(sd);
        });
        signUpButton.setOnMouseExited(event -> {
            signUpButton.setScaleX(1.0);
        });
        signUpButton.setStyle("-fx-font-family: 'Times New Roman';" +      // Set font family
                "-fx-font-size: 12pt;" +           // Set font size
                "-fx-text-fill: white;" +         // Set text color
                "-fx-background-color: #5876e3;-fx-font-weight: bolder");


        HBox sloganBox = new HBox();
        sloganBox.getChildren().add(titleLabel);
        sloganBox.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox();
        buttonBox.getChildren().addAll(signUpButton,new Label());
        buttonBox.setAlignment(Pos.CENTER);

        TextField passtextfield=new TextField();
        passtextfield.setStyle(     // Set font family// Set font size
                "-fx-font-family: 'Times New Roman';-fx-font-size: 16");
//        passtextfield.setPrefWidth(20);
        passtextfield.setVisible(false);

        Hyperlink showpassword=new Hyperlink();
//        Image closedeyeIcon = new Image(getClass().getResource("/images/closedeye.png").toString()); // Create a BackgroundImage with the loaded image
        ImageView closedeyeIcon = UIUtils.loadExternalImageView("/images/closedeye.png",25,25);

        showpassword.setGraphic(closedeyeIcon);
        showpassword.setStyle("-fx-font-size: 13;-fx-font-weight: bolder;");

        showpassword.setOnAction(ev ->{
            isPasswordVisible=!isPasswordVisible;
            if (isPasswordVisible){
                passtextfield.setText(passwordField.getText());
                passtextfield.setVisible(true);
                passwordField.setVisible(false);
//                showpassword.setText("Hide");

//                Image openeyeIcon = new Image(getClass().getResource("/images/openeye.png").toString()); // Create a BackgroundImage with the loaded image
                ImageView openeyeIcon = UIUtils.loadExternalImageView("/images/openeye.png",25,25);

                showpassword.setGraphic(openeyeIcon);

            }
            else {
                passwordField.setText(passtextfield.getText());
                passtextfield.setVisible(false);
                passwordField.setVisible(true);
//                showpassword.setText("Show");

//                Image closedeyeIcon2 = new Image(getClass().getResource("/images/closedeye.png").toString()); // Create a BackgroundImage with the loaded image
                ImageView closedeyeIcon2 = UIUtils.loadExternalImageView("/images/closedeye.png",25,25);

                showpassword.setGraphic(closedeyeIcon2);

            }
        });
        StackPane passwordStack=new StackPane(passwordField,passtextfield);
        passwordStack.setMaxWidth(250);


        HBox passwordBox = new HBox(20);
        passwordBox.getChildren().addAll( passwordStack,showpassword);
        VBox layout = new VBox(10);
// Spacing between elements
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll( sloganBox,new Separator(),usernameLabel, usernameField,passwordLabel,passwordBox,schoolNameLabel,schoolNameField,new Label(),buttonBox );
//        layout.getStyleClass().add("form-layout");
        // Register button event handler
//        signUpButton.setOnAction(event -> {
//
//            if (!usernameField.getText().isBlank()&&!passwordField.getText().isBlank()) {
//                layout.setDisable(true);
//                usernameLabel.setDisable(true);
//                usernameField.setDisable(true);
//                passwordLabel.setDisable(true);
//                passwordField.setDisable(true);
//                signUpButton.setDisable(true);
//
//                showLoadingPopUp(primaryStage);
//                // Get username and password from the text fields
//                String username = usernameField.getText();
//                String password = passwordField.getText();
//                String school=schoolNameField.getText();
//                // Save username and password to the user table in the database
//                db.saveUserToDatabase(username, password,school);
//            }
//
//        });

        signUpButton.setOnAction(event -> {
            if (!usernameField.getText().isBlank() && !passwordField.getText().isBlank()) {

                layout.setDisable(true);
                usernameLabel.setDisable(true);
                usernameField.setDisable(true);
                passwordLabel.setDisable(true);
                passwordField.setDisable(true);
                signUpButton.setDisable(true);

                // Get user input
                String username = usernameField.getText();
                String password = passwordField.getText();
                String school = schoolNameField.getText();

                // Attempt to save user and get back result
                boolean success = db.saveUserToDatabase(username, password, school);

                if (success) {
                    showLoadingPopUp(primaryStage);
                } else {
                    // Optional: log, show silent failure, or re-enable UI
                    layout.setDisable(false);
                    usernameLabel.setDisable(false);
                    usernameField.setDisable(false);
                    passwordLabel.setDisable(false);
                    passwordField.setDisable(false);
                    signUpButton.setDisable(false);
                }
            }
        });


        layout.setStyle("-fx-background-color: #ECF0F1;"); // Change background color here

        titleLabel.setId("titleLabel");
        sloganBox.setId("sloganBox");
        buttonBox.setId("buttonBox");

        usernameLabel.getStyleClass().add("label");
        usernameField.getStyleClass().add("text-field");
        signUpButton.getStyleClass().add("button");


        // Set scene and show stage
        Scene scene = new Scene(layout, 355, 420);
        scene.getStylesheets().add(getClass().getResource("/signup.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void showLoadingPopUp(Stage primarystage) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.TRANSPARENT);
        ProgressIndicator progressIndicator=new ProgressIndicator();
        progressIndicator.setMinSize(75,75);

//        progressIndicator.setStyle("-fx-text-fill: blue");
        Label label=new Label("Saving User Data");
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


                StartupPreviewWindow startupwindow = new StartupPreviewWindow();
                Stage pStage=new Stage();
                startupwindow.start(pStage);
            });
        }).start();

    }



    public static void main(String[] args) {
        launch(args);
    }
}
