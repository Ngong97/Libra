package com.ngong.librasoftware.utils;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class WarningMessage extends StackPane {

    public WarningMessage(String message, Duration displayTime, Pane hostContainer) {
        Label msgLabel = new Label(message);
        msgLabel.getStyleClass().add("warning-message-label");

        this.getChildren().add(msgLabel);
        this.getStyleClass().add("warning-message-container");
        this.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        this.setMaxWidth(300);
        this.setMaxHeight(90);
        msgLabel.setWrapText(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(displayTime);

        fadeIn.setOnFinished(e -> fadeOut.play());
        fadeOut.setOnFinished(e -> hostContainer.getChildren().remove(this)); // Use host reference safely

        fadeIn.play();
    }
}
