package com.ngong.librasoftware.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ErrorNotifier {

    public static void show(String context, Exception e) {
        String message = "⚠ " + context + ": " + e.getMessage();

        Stage popup = new Stage();
        popup.initStyle(StageStyle.TRANSPARENT);
        popup.setAlwaysOnTop(true);

        Label label = new Label(message);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10px;");

        StackPane root = new StackPane(label);
        root.setBackground(new Background(new BackgroundFill(Color.web("#B22222"), new CornerRadii(8), Insets.EMPTY)));
        root.setEffect(new DropShadow(8, Color.gray(0.3)));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.setWidth(360);
        popup.setHeight(50);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        popup.setX(bounds.getMaxX() - popup.getWidth() - 20);
        popup.setY(bounds.getMaxY() - popup.getHeight() - 20);

        popup.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition stay = new PauseTransition(Duration.seconds(5));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e2 -> popup.close());

        new SequentialTransition(fadeIn, stay, fadeOut).play();
    }
}

