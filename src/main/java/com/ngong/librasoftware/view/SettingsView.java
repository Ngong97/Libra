package com.ngong.librasoftware.view;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.utils.AnimationUtils;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SettingsView extends BorderPane {
    private final DatabaseService db = new DatabaseService();

    private final VBox parentBox = new VBox(20);

    public SettingsView() {
        this.getStyleClass().add("settings-root");

        Label heading = new Label("⚙️ System Settings");
        heading.getStyleClass().add("settings-heading");

        // Left Navigation
        VBox navMenu = new VBox(10);
        navMenu.getStyleClass().add("settings-nav");

        Button accountBtn = new Button("👤 Account");
        accountBtn.setCursor(Cursor.HAND);
        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency

        DropShadow blueShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency

        accountBtn.setEffect(blackShadow);
        accountBtn.setOnMouseEntered(event -> {
            accountBtn.setScaleX(1.1);
            accountBtn.setEffect(blueShadow);
        });
        accountBtn.setOnMouseExited(event -> {
            accountBtn.setScaleX(1.0);
            accountBtn.setEffect(blackShadow);
        });
        ScaleTransition pressUpdate = new ScaleTransition(Duration.millis(80), accountBtn);
        pressUpdate.setToX(0.95);
        pressUpdate.setToY(0.95);

        ScaleTransition releaseUpdate = new ScaleTransition(Duration.millis(80), accountBtn);
        releaseUpdate.setToX(1.0);
        releaseUpdate.setToY(1.0);

        accountBtn.setOnMousePressed(e -> pressUpdate.play());
        accountBtn.setOnMouseReleased(e -> releaseUpdate.play());

        Button dbBtn = new Button("🗃️ Database");

        dbBtn.setCursor(Cursor.HAND);


        dbBtn.setEffect(blackShadow);
        dbBtn.setOnMouseEntered(event -> {
            dbBtn.setScaleX(1.1);
            dbBtn.setEffect(blueShadow);
        });
        dbBtn.setOnMouseExited(event -> {
            dbBtn.setScaleX(1.0);
            dbBtn.setEffect(blackShadow);
        });
        ScaleTransition pressDB = new ScaleTransition(Duration.millis(80), dbBtn);
        pressDB.setToX(0.95);
        pressDB.setToY(0.95);

        ScaleTransition releaseDB = new ScaleTransition(Duration.millis(80), dbBtn);
        releaseDB.setToX(1.0);
        releaseDB.setToY(1.0);

        dbBtn.setOnMousePressed(e -> pressDB.play());
        dbBtn.setOnMouseReleased(e -> releaseDB.play());


        navMenu.getChildren().addAll(accountBtn, dbBtn);

        // Right Content Area
        parentBox.setPadding(new Insets(20));
        parentBox.setPrefWidth(500);

        // Default view
        showAccountSettings();

        // Navigation actions
        accountBtn.setOnAction(e -> showAccountSettings());
        dbBtn.setOnAction(e -> showDatabaseSettings());

        // Layout
        HBox mainLayout = new HBox(navMenu, new Separator(), parentBox);
        mainLayout.setSpacing(20);
        mainLayout.setPadding(new Insets(20));

        VBox wrapper = new VBox(heading, mainLayout);
        wrapper.setSpacing(10);
        wrapper.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(wrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        this.setCenter(scrollPane);
        this.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        AnimationUtils.applyFadeIn(this, 600);
    }

    private void showAccountSettings() {
        parentBox.getChildren().clear();

        String currentName = db.getCurrentUserFullName();
        String currentPassword = db.getCurrentUserPassword();

        TextField nameField = new TextField();
        nameField.setMaxWidth(300);
        nameField.setPromptText("Enter your new name");
        nameField.getStyleClass().add("user-textfield");
        nameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F3 && event.isShiftDown()) {
                IndexRange selection = nameField.getSelection();

                if (selection.getLength() > 0) {
                    String selectedText = nameField.getSelectedText();
                    String capitalized = capitalizeEachWord(selectedText);

                    // Replace selection with capitalized version
                    StringBuilder newText = new StringBuilder(nameField.getText());
                    newText.replace(selection.getStart(), selection.getEnd(), capitalized);
                    nameField.setText(newText.toString());

                    // Reselect the updated portion
                    nameField.selectRange(selection.getStart(), selection.getStart() + capitalized.length());
                }
            }
        });



        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(300);
        passwordField.setPromptText("Enter your new password");
        passwordField.getStyleClass().add("user-textfield");

        Label currentNameLabel=new Label("Name: ");
        Label currentPasswordLabel=new Label("Password: ");





        Button saveBtn = new Button("💾 Save");
        saveBtn.setCursor(Cursor.HAND);
        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency

        DropShadow blueShadow = new DropShadow();
        blueShadow.setOffsetY(2.0);
        blueShadow.setColor(Color.BLUE); // Set shadow color and transparency

        saveBtn.setEffect(blackShadow);
        saveBtn.setOnMouseEntered(event -> {
            saveBtn.setScaleX(1.1);
            saveBtn.setEffect(blueShadow);
        });
        saveBtn.setOnMouseExited(event -> {
            saveBtn.setScaleX(1.0);
            saveBtn.setEffect(blackShadow);
        });
        ScaleTransition pressSave = new ScaleTransition(Duration.millis(80), saveBtn);
        pressSave.setToX(0.95);
        pressSave.setToY(0.95);

        ScaleTransition releaseSave = new ScaleTransition(Duration.millis(80), saveBtn);
        releaseSave.setToX(1.0);
        releaseSave.setToY(1.0);

        saveBtn.setOnMousePressed(e -> pressSave.play());
        saveBtn.setOnMouseReleased(e -> releaseSave.play());
        saveBtn.getStyleClass().add("settings-button");

        saveBtn.setOnAction(e -> {
            String newName = nameField.getText().trim();
            String newPassword = passwordField.getText().trim();
            if (!newName.isEmpty() && !newPassword.isEmpty()) {
                boolean updated = db.updateUserInfo(newName, newPassword);
                if (updated) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText(null);
                alert.setContentText("Updated successfully!");
                alert.showAndWait();
                }
            }
            else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText(null);
                alert.setContentText("Please provide recovery details!");
                alert.showAndWait();
            }

        });


        HBox CurrentNameBox=new HBox(currentNameLabel,new Label(currentName));
        HBox CurrentPasswordBox=new HBox(currentPasswordLabel,new Label(currentPassword));



        Button resetBtn = createResetButton(() -> {
            nameField.setText("");
            passwordField.setText("");
        });


        Label nameFieldLabel=new Label("Username:");
        nameFieldLabel.getStyleClass().add("settings-label");
        Label passwordFieldLabel=new Label("Password:");
        passwordFieldLabel.getStyleClass().add("settings-label");


        Label currentIndicator=new Label("Current Settings: ");
        currentIndicator.setStyle("-fx-font-style: italic;-fx-font-weight: bold;");

        HBox currentBox=new HBox(currentIndicator,CurrentNameBox,CurrentPasswordBox);
        currentBox.setSpacing(10);
        parentBox.getChildren().addAll(
                createSectionTitle("👤 Account Settings"),
                nameFieldLabel,nameField, passwordFieldLabel,passwordField,
                new HBox(10, saveBtn, resetBtn),
                currentBox
        );
        this.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

    }

    private String capitalizeEachWord(String input) {
        String[] words = input.trim().split("\\s+");
        return Arrays.stream(words)
                .map(word -> word.isEmpty() ? word :
                        Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }


    private void showDatabaseSettings() {
        parentBox.getChildren().clear();

        // 📁 Backup Local Database
        Label backupLabel = new Label("Backup Local Database:");
        Button backupBtn = new Button("Backup Database");
        backupBtn.setCursor(Cursor.HAND);
        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency

        DropShadow blueShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency

        backupBtn.setEffect(blackShadow);
        backupBtn.setOnMouseEntered(event -> {
            backupBtn.setScaleX(1.1);
            backupBtn.setEffect(blueShadow);
        });
        backupBtn.setOnMouseExited(event -> {
            backupBtn.setScaleX(1.0);
            backupBtn.setEffect(blackShadow);
        });
        ScaleTransition pressUpdate = new ScaleTransition(Duration.millis(80), backupBtn);
        pressUpdate.setToX(0.95);
        pressUpdate.setToY(0.95);

        ScaleTransition releaseUpdate = new ScaleTransition(Duration.millis(80), backupBtn);
        releaseUpdate.setToX(1.0);
        releaseUpdate.setToY(1.0);

        backupBtn.setOnMousePressed(e -> pressUpdate.play());
        backupBtn.setOnMouseReleased(e -> releaseUpdate.play());


        backupBtn.getStyleClass().add("settings-button");

        backupBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Backup Local Database");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.setInitialFileName("libra-backup.db");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("SQLite DB", "*.db")
            );

            String defaultFileName = "libra_backup";
            File defaultDir = new File(System.getProperty("user.home"), "Documents");
            fileChooser.setInitialDirectory(defaultDir.exists() ? defaultDir : new File(System.getProperty("user.home")));
            fileChooser.setInitialFileName(defaultFileName + ".db");



            File destination = fileChooser.showSaveDialog(this.getScene().getWindow());

            if (destination != null) {
                File sourceDb = new File(System.getProperty("user.home"), "libraDB/libra.db");
                if (!sourceDb.exists()) {
                    new Alert(Alert.AlertType.ERROR, "Local database not found at:\n" + sourceDb.getAbsolutePath()).show();
                    return;
                }
                try {
                    Files.copy(sourceDb.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to back up database:\n" + ex.getMessage()).show();
                }
            }
        });





        VBox backupBox = new VBox(10, backupLabel, backupBtn);
        backupBox.setPadding(new Insets(10));

        parentBox.getChildren().addAll(
                createSectionTitle("🗃️ Database Settings"),
                backupBox
        );

    }


    private void showTransientPopup(String message, Duration duration) {
        Label popupLabel = new Label(message);
        popupLabel.setStyle("""
        -fx-background-color: #333;
        -fx-text-fill: white;
        -fx-padding: 12 24;
        -fx-background-radius: 10;
        -fx-font-size: 15px;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10, 0.5, 0, 2);
    """);

        // Wrap in StackPane to ensure centering
        StackPane overlay = new StackPane(popupLabel);
        overlay.setAlignment(Pos.CENTER);
        overlay.setMouseTransparent(true);

        // Ensure overlay fills available space
        overlay.setPrefSize(this.getWidth(), this.getHeight());
        overlay.setOpacity(0);

        this.getChildren().add(overlay);

        // Fade in and then out
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), overlay);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        FadeTransition fadeOut = new FadeTransition(duration, overlay);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> this.getChildren().remove(overlay));

        SequentialTransition sequence = new SequentialTransition(fadeIn, fadeOut);
        sequence.play();
    }


    private Label createSectionTitle(String title) {
        Label label = new Label(title);
        label.getStyleClass().add("section-title");
        return label;
    }

    private Button createResetButton(Runnable action) {
        Button resetBtn = new Button("↩️ Reset");
        resetBtn.setCursor(Cursor.HAND);
        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency

        DropShadow blueShadow = new DropShadow();
        blueShadow.setOffsetY(2.0);
        blueShadow.setColor(Color.BLUE); // Set shadow color and transparency

        resetBtn.setEffect(blackShadow);
        resetBtn.setOnMouseEntered(event -> {
            resetBtn.setScaleX(1.1);
            resetBtn.setEffect(blueShadow);
        });
        resetBtn.setOnMouseExited(event -> {
            resetBtn.setScaleX(1.0);
            resetBtn.setEffect(blackShadow);
        });
        ScaleTransition pressUpdate = new ScaleTransition(Duration.millis(80), resetBtn);
        pressUpdate.setToX(0.95);
        pressUpdate.setToY(0.95);

        ScaleTransition releaseUpdate = new ScaleTransition(Duration.millis(80), resetBtn);
        releaseUpdate.setToX(1.0);
        releaseUpdate.setToY(1.0);

        resetBtn.setOnMousePressed(e -> pressUpdate.play());
        resetBtn.setOnMouseReleased(e -> releaseUpdate.play());
        resetBtn.getStyleClass().add("settings-button");
        resetBtn.setOnAction(e -> action.run());
        return resetBtn;
    }

}
