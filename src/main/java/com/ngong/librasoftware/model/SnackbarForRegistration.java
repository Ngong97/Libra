package com.ngong.librasoftware.model;

import javafx.animation.FadeTransition;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class SnackbarForRegistration {
    private final StackPane parent;
    private final HBox snackbarBox;
    public SnackbarForRegistration(StackPane parent) {
        this.parent = parent;
        snackbarBox = new HBox();
        snackbarBox.setStyle("-fx-background-color: #a9aeba; -fx-padding: 10; -fx-background-radius: 5;");
        snackbarBox.setOpacity(0); // Start hidden  a9aeba
        parent.getChildren().add(snackbarBox);
    }
    public void show(String message) {
        snackbarBox.getChildren().clear();
        Text text = new Text(message);
        text.setFill(Color.valueOf("#020202"));
        text.setFont(Font.font("Consolas", FontWeight.BOLD,20));

        snackbarBox.getChildren().add(text); // Set the size and position
        snackbarBox.setMaxWidth(160);
        snackbarBox.setTranslateY(parent.getHeight() - 350); // Adjust for desired position // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), snackbarBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play(); // Fade out after a delay
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), snackbarBox);
        fadeOut.setDelay(Duration.seconds(2)); // Show for 2 seconds

        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> snackbarBox.setVisible(false)); // Hide after fading out
        fadeIn.setOnFinished(event -> { snackbarBox.setVisible(true); // Show snackbar after fade in
            fadeOut.play();
        });
        fadeIn.play();
    }
}