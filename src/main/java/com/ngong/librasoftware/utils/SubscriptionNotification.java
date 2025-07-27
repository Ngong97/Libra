package com.ngong.librasoftware.utils;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
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

public class SubscriptionNotification {

    public static void show(Stage owner, Runnable onClick) {
        Stage popup = new Stage();
        popup.initStyle(StageStyle.TRANSPARENT);
        popup.setAlwaysOnTop(true);
        popup.initOwner(owner);

        Label label = new Label("Subscribe for future updates");
        label.setCursor(Cursor.HAND);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12px;");
        label.setOnMouseClicked(e -> {
            popup.close();
            onClick.run();
        });

        StackPane root = new StackPane(label);
        root.setBackground(new Background(new BackgroundFill(Color.web("#2E8B57"), new CornerRadii(10), Insets.EMPTY)));
        root.setEffect(new DropShadow(10, Color.gray(0, 0.4)));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.setWidth(250);
        popup.setHeight(60);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double startX = bounds.getMaxX();
        double targetX = bounds.getMaxX() - popup.getWidth() - 20;
        double targetY = bounds.getMaxY() - popup.getHeight() - 40;

        popup.setX(startX);
        popup.setY(targetY);
        popup.show();

        Timeline slideIn = new Timeline(
                new KeyFrame(Duration.ZERO, e -> popup.setX(startX)),
                new KeyFrame(Duration.millis(400), e -> popup.setX(targetX))
        );

        PauseTransition hold = new PauseTransition(Duration.seconds(6));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> popup.close());

        new SequentialTransition(slideIn, hold, fadeOut).play();
    }
}
