package com.ngong.librasoftware.view;

import com.ngong.librasoftware.DAO.DatabaseService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PasswordRecoveryForm extends Stage {
    private final DatabaseService db = new DatabaseService();

    public PasswordRecoveryForm() {
        this.setTitle("🔑 Password Recovery");
        this.initModality(Modality.APPLICATION_MODAL);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f7f9fc;");

        Label title = new Label("🔒 Reset Your Password");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("👤 Librarian Name");
        usernameField.setMaxWidth(300);
        usernameField.getStyleClass().add("recovery-input");

        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("🔐 New Password");
        newPassField.setMaxWidth(300);
        newPassField.getStyleClass().add("recovery-input");

        Label feedback = new Label("");
        feedback.setWrapText(true);
        feedback.setMaxWidth(320);
        feedback.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 13px;");

        Button submitBtn = new Button("✅ Reset Password");
        submitBtn.setDefaultButton(true);
        submitBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20;");

        submitBtn.setOnAction(e -> {
            String user = usernameField.getText().trim();
            String newPass = newPassField.getText().trim();

            if (user.isEmpty() || newPass.isEmpty()) {
                feedback.setText("⚠️ Both fields are required.");
                return;
            }

            if (db.userExists(user)) {
                boolean updated = db.updateUserPassword(user, newPass);
                if (updated) {
                    feedback.setText("✅ Password updated successfully.");
                    feedback.setStyle("-fx-text-fill: #388e3c;");
                    new Timeline(new KeyFrame(Duration.seconds(2), ev -> this.close())).play();
                } else {
                    feedback.setText("❌ Update failed.");
                }
            } else {
                feedback.setText("👤 No matching librarian found.");
            }
        });

        layout.getChildren().addAll(title, usernameField, newPassField, submitBtn, feedback);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(getClass().getResource("/recovery.css").toExternalForm());
        this.setScene(scene);
        this.setWidth(400);
    }

    public void launch() {
        this.show();
    }
}

