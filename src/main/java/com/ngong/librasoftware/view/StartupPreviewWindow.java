package com.ngong.librasoftware.view;

import com.ngong.librasoftware.Controller.DashboardApp;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class StartupPreviewWindow extends Application {
    private String fetchSchoolName() {
        File activeDb = new File(System.getProperty("user.home"), "libraDB/libra.db");
        if (!activeDb.exists()) return "Libra Library";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + activeDb.getAbsolutePath());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT school FROM users LIMIT 1")) {
            return rs.next() ? rs.getString("school") : "Libra Library";
        } catch (Exception ex) {
            return "Libra Library";
        }
    }

    @Override
    public void start(Stage stage) {
        String schoolName = fetchSchoolName();

        Label schoolLabel = new Label(schoolName);
        schoolLabel.getStyleClass().add("school-label");

        Label tagline = new Label("Welcome to Libra — your trusted library system");
        tagline.getStyleClass().add("tagline");

        Button enterBtn = new Button("📚 Enter Dashboard");
        enterBtn.getStyleClass().add("enter-button");
        enterBtn.setOnAction(e -> {
            stage.close();
            new DashboardApp().start(new Stage());
        });

        Label footer = new Label("Libra Software © 2025");
        footer.getStyleClass().add("footer-label");

        VBox content = new VBox(20, schoolLabel, tagline, enterBtn, footer);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(60));

        Scene scene = new Scene(content, 500, 400);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Libra — Startup");
        stage.show();
    }
}
