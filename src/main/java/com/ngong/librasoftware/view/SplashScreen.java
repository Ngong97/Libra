package com.ngong.librasoftware.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreen extends Application {

    private final int DOT_COUNT = 12;
    private final Circle[] dots = new Circle[DOT_COUNT];
    private int currentIndex = 0;

    @Override
    public void start(Stage primaryStage) {
        // Book icon
        Label icon = new Label("\uD83D\uDCD6"); // 📖 emoji
        icon.setId("icon");

        Label title = new Label("LIBRA");
        title.setId("title");

        Label subtitle = new Label("LIBRARY MANAGEMENT SYSTEM");
        subtitle.setId("subtitle");

        Label version = new Label("v2.0");
        version.setId("version");

        Label loading = new Label("Loading...");
        loading.setId("loading");

        StackPane spinner = buildCircularSpinner();

        VBox content = new VBox(20, icon, title, subtitle, version, spinner, loading);
        content.setAlignment(Pos.CENTER);
        content.setId("splash-root");

        Scene scene = new Scene(content, 600, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.show();

        animateSpinner();

        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(8), e -> {
            primaryStage.close();
            launchMainApp();
        }));
        delay.play();
    }

    private StackPane buildCircularSpinner() {
        StackPane spinner = new StackPane();
        spinner.setPrefSize(50, 50);

        double radius = 25;
        for (int i = 0; i < DOT_COUNT; i++) {
            double angle = 2 * Math.PI * i / DOT_COUNT;
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);
            Circle dot = new Circle(3, Color.web("#3c4c60"));
            dot.setTranslateX(x);
            dot.setTranslateY(y);
            dots[i] = dot;
            spinner.getChildren().add(dot);
        }

        return spinner;
    }

    private void animateSpinner() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            for (int i = 0; i < DOT_COUNT; i++) {
                double opacity = (i == currentIndex) ? 1.0 :
                        (i == (currentIndex + 1) % DOT_COUNT) ? 0.7 :
                                (i == (currentIndex + 2) % DOT_COUNT) ? 0.4 : 0.2;
                dots[i].setFill(Color.WHITE);
                dots[i].setOpacity(opacity);
            }
            currentIndex = (currentIndex + 1) % DOT_COUNT;
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void launchMainApp() {
        LoginOrRegister loginOrRegister=new LoginOrRegister();
        loginOrRegister.checkAndProceed();
//        ((Stage) progressBar.getScene().getWindow()).close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


