package com.ngong.librasoftware.Controller;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.view.LoginOrRegister;
import com.ngong.librasoftware.view.SignUpWindow;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashWindow extends Application {
    private final DatabaseService db = new DatabaseService();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.UNDECORATED); // Remove window decorations
        primaryStage.setTitle("Libra - Splash Screen");

        // Create layout for text and progress on the left
        VBox leftLayout = new VBox(20);
        leftLayout.setAlignment(Pos.CENTER);

        // Title label
        Label titleLabel = new Label("Libra");
        titleLabel.setFont(new Font("Segoe UI Bold", 48));
        titleLabel.setTextFill(Color.web("#333333")); // Dark color for title

        // Initializing label
        Label initializingLabel = new Label("Initializing...");
        initializingLabel.setFont(new Font("Segoe UI", 20));
        initializingLabel.setTextFill(Color.web("#555555")); // Medium gray for text

        // Progress bar
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(12);
        progressBar.setStyle("-fx-accent: #0c66ca;");  // Orange accent color


        // Version label
        Label versionLabel = new Label("v1.5");
        versionLabel.setFont(new Font("Segoe UI", 14));
        versionLabel.setTextFill(Color.web("#888888"));  // Lighter gray for version text
        versionLabel.setAlignment(Pos.CENTER_RIGHT);

        leftLayout.getChildren().addAll(titleLabel,new Label(),progressBar,initializingLabel,versionLabel);

        // Image on the right
        Image image = new Image(getClass().getResource("/images/library_image.jpg").toExternalForm()); // Your image path
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true); // Maintain aspect ratio
        imageView.setFitWidth(400); // Set to fill the right part
        imageView.setFitHeight(400);

        StackPane rightLayout = new StackPane(imageView);
        rightLayout.setAlignment(Pos.CENTER);
        rightLayout.setPrefWidth(400); // Adjust width as needed to balance left and right parts

        // Create HBox for split layout
        HBox root = new HBox();
        root.getChildren().addAll(leftLayout, rightLayout);


        // Set the scene
        Scene scene = new Scene(root, 600, 250); // Adjust width/height as needed
        primaryStage.setScene(scene);
        primaryStage.show();

        // Fill the progress bar over time
//        fillProgressBar(progressBar, progressPercentage, 8000);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                long startTime = System.currentTimeMillis();
                long endTime = startTime + 8000;

                while (System.currentTimeMillis() < endTime) {

                    Thread.sleep(50); // Control the update rate
                }
                updateProgress(1, 1); // Ensure progress is at 100% when done
                updateMessage("100%"); // Set the final progress percentage
                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());

        new Thread(task).start();

        task.setOnSucceeded(event -> {
            LoginOrRegister loginOrRegister=new LoginOrRegister();
            loginOrRegister.checkAndProceed();
            ((Stage) progressBar.getScene().getWindow()).close();
        });
    }

    private void fillProgressBar(ProgressBar progressBar, Label progressPercentage, long durationMillis) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                long startTime = System.currentTimeMillis();
                long endTime = startTime + durationMillis;

                while (System.currentTimeMillis() < endTime) {
                    double progress = (System.currentTimeMillis() - startTime) / (double) durationMillis;
                    updateProgress(progress, 1);

                    // Update the progress count label
                    int percent = (int) (progress * 100);
                    updateMessage(percent + "%");
                    Thread.sleep(50); // Control the update rate
                }
                updateProgress(1, 1); // Ensure progress is at 100% when done
                updateMessage("100%"); // Set the final progress percentage
                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        progressPercentage.textProperty().bind(task.messageProperty()); // Bind the progress count label to task

        new Thread(task).start();

        task.setOnSucceeded(event -> {
            SignUpWindow signup = new SignUpWindow();
            Stage pStage=new Stage();
            signup.start(pStage);
            ((Stage) progressBar.getScene().getWindow()).close();
        });
    }



    public static void main(String[] args) {
        launch(args);
    }
}
