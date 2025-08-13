package com.ngong.librasoftware.view;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.utils.AnimationUtils;
import com.ngong.librasoftware.utils.NotificationUtil;
import com.ngong.librasoftware.utils.TrialManager;
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
import java.nio.file.*;
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
        HBox navMenu = new HBox(10);
        navMenu.getStyleClass().add("settings-nav");

        Hyperlink accountBtn = new Hyperlink("👤 Account");
        accountBtn.setCursor(Cursor.HAND);
        accountBtn.getStyleClass().add("diff-button");
        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency

        DropShadow blueShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency

        ScaleTransition pressUpdate = new ScaleTransition(Duration.millis(80), accountBtn);
        pressUpdate.setToX(0.95);
        pressUpdate.setToY(0.95);

        ScaleTransition releaseUpdate = new ScaleTransition(Duration.millis(80), accountBtn);
        releaseUpdate.setToX(1.0);
        releaseUpdate.setToY(1.0);

        accountBtn.setOnMousePressed(e -> pressUpdate.play());
        accountBtn.setOnMouseReleased(e -> releaseUpdate.play());

        Hyperlink dbBtn = new Hyperlink("🗃️ Database");
        dbBtn.getStyleClass().add("diff-button");
        dbBtn.setCursor(Cursor.HAND);


        ScaleTransition pressDB = new ScaleTransition(Duration.millis(80), dbBtn);
        pressDB.setToX(0.95);
        pressDB.setToY(0.95);

        ScaleTransition releaseDB = new ScaleTransition(Duration.millis(80), dbBtn);
        releaseDB.setToX(1.0);
        releaseDB.setToY(1.0);

        dbBtn.setOnMousePressed(e -> pressDB.play());
        dbBtn.setOnMouseReleased(e -> releaseDB.play());



        // Right Content Area
        parentBox.setPadding(new Insets(20));
        parentBox.setPrefWidth(500);





//        BorderPane myborderpane=showAccountSettings();


        TitledPane accountPane = new TitledPane("Account Settings", showAccountSettings());
        accountPane.setExpanded(false); // collapsed by default
        accountPane.setMaxWidth(400);


        TitledPane databasePane = new TitledPane("Database Settings", showDatabaseSettings());
        databasePane.setExpanded(false); // collapsed by default
        databasePane.setMaxWidth(400);

        TitledPane activationPane = new TitledPane("Activation Settings", showActivationSettings());
        activationPane.setExpanded(false); // collapsed by default
        activationPane.setMaxWidth(400);

        navMenu.getChildren().addAll(accountPane, databasePane,activationPane);


        // Layout
        HBox mainLayout = new HBox(navMenu);
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

    private VBox showAccountSettings() {

        VBox borderPane = new VBox();
        borderPane.setPadding(new Insets(20));
        borderPane.setSpacing(10);
//        parentBox.getChildren().clear();

        String currentName = db.getCurrentUserFullName();
        String currentPassword = db.getCurrentUserPassword();

        TextField nameField = new TextField();
        nameField.setMaxWidth(300);
        nameField.setPromptText("Enter your new name");
        nameField.getStyleClass().add("recovery-input");
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
        passwordField.getStyleClass().add("recovery-input");

        Label currentNameLabel=new Label("Name: ");
        Label currentPasswordLabel=new Label("Password: ");





        Button saveBtn = new Button("💾 Save");
        saveBtn.setCursor(Cursor.HAND);
        saveBtn.setStyle("-fx-font-weight: bold");
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
        borderPane.getChildren().addAll(
                createSectionTitle("👤 Account Settings"),
                nameFieldLabel,nameField, passwordFieldLabel,passwordField,
                new HBox(20, saveBtn, resetBtn),
                currentBox
        );
        this.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        this.getStylesheets().add(getClass().getResource("/recovery.css").toExternalForm());

        return borderPane;
    }

    private String capitalizeEachWord(String input) {
        String[] words = input.trim().split("\\s+");
        return Arrays.stream(words)
                .map(word -> word.isEmpty() ? word :
                        Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }


    private VBox showDatabaseSettings() {
//        parentBox.getChildren().clear();
        VBox borderPane= new VBox();
        borderPane.setPadding(new Insets(20));
        borderPane.setSpacing(10);

        Label backupLabel = new Label("Backup Local Database:");
        Button backupBtn = new Button("Backup Database");
        backupBtn.setCursor(Cursor.HAND);
        backupBtn.setStyle("-fx-font-weight: bold;");
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
                File sourceDb = new File(System.getProperty("user.home"), "libraDB/currentDB/libra.db");
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


            Path recoveryFolder = Paths.get(System.getProperty("user.home"), "LDB_recoveryTool");
            try {
                Files.createDirectories(recoveryFolder); // Ensure folder is created
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            String batchContent = """
                    @echo off
                    echo.
                    echo 🔒 Please make sure Libra is CLOSED before continuing.
                    echo If it's still running, the database will be locked and replacement will FAIL.
                    pause
                    
                    setlocal
                    set "userProfile=%USERPROFILE%"
                    set "currentDB=%userProfile%\\LibraDB\\currentDB\\libra.db"
                    set "backupDB=%userProfile%\\LibraDB\\backupDB\\newDB.db"
                    set "timestamp=%DATE:/=-%_%TIME::=-%"
                    set "backupDir=%userProfile%\\LibraDB\\currentDB\\backups"
                    
                    echo.
                    echo Do you want to back up the current database before replacing? (Y/N)
                    choice /c YN /n /m "[Y]es or [N]o: "
                    set choice=%ERRORLEVEL%
                    
                    if %choice%==1 (
                        echo Backing up current DB...
                        if not exist "%backupDir%" mkdir "%backupDir%"
                        copy "%currentDB%" "%backupDir%\\libra_backup_%timestamp%.db"
                        echo ✅ Backup saved to: %backupDir%\\libra_backup_%timestamp%.db
                    )
                    
                    echo Replacing current database with new version...
                    copy /Y "%backupDB%" "%currentDB%"
                    
                    if %ERRORLEVEL%==0 (
                        echo ✅ Database successfully replaced.
                    ) else (
                        echo ❌ Replacement failed. Is Libra still running? Are the paths correct?
                    )
                    
                    pause
                    endlocal
                    
            """;

            Path batchPath = recoveryFolder.resolve("replaceDB.bat");

            try {
                Files.writeString(batchPath, batchContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                new Alert(Alert.AlertType.INFORMATION,"🛠️ Recovery tool saved at: " + batchPath).show();
            } catch (IOException ev) {
                ev.printStackTrace();
            }

        });

        VBox backupBox = new VBox(10, backupLabel, new HBox(20,backupBtn));
        backupBox.setPadding(new Insets(10));

        borderPane.getChildren().addAll(
                createSectionTitle("🗃️ Database Settings"),
                backupBox
        );

        return borderPane;
    }


    public VBox showActivationSettings() {
        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency

        DropShadow blueShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency



        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.TOP_LEFT);
        box.getStyleClass().add("settings-section");

        Label heading = new Label("🔓 Trial Activation");
        heading.getStyleClass().add("section-heading");

        Label status = new Label();
        status.setWrapText(true);
        status.getStyleClass().add("info-text");

        if (db.isActivated()) {
            status.setText("✅ Your Libra trial is activated.");
        } else {
            int daysLeft = TrialManager.getRemainingDays();
            status.setText("⏳ Trial active — " + daysLeft + " day" + (daysLeft == 1 ? "" : "s") + " remaining.");
        }

        TextField keyField = new TextField();
        keyField.setPromptText("Enter activation code");
        keyField.getStyleClass().add("recovery-input");
        keyField.setPrefWidth(250);

        Button activateBtn = new Button("Activate");
        activateBtn.getStyleClass().add("settings-button");
        activateBtn.setCursor(Cursor.HAND);
        activateBtn.setOnMouseEntered(event -> {
            activateBtn.setScaleX(1.1);
            activateBtn.setEffect(blueShadow);
        });
        activateBtn.setOnMouseExited(event -> {
            activateBtn.setScaleX(1.0);
            activateBtn.setEffect(blackShadow);
        });
        ScaleTransition pressExport = new ScaleTransition(Duration.millis(80), activateBtn);
        pressExport.setToX(0.95);
        pressExport.setToY(0.95);

        ScaleTransition releaseExport = new ScaleTransition(Duration.millis(80), activateBtn);
        releaseExport.setToX(1.0);
        releaseExport.setToY(1.0);

        activateBtn.setOnMousePressed(e -> pressExport.play());
        activateBtn.setOnMouseReleased(e -> releaseExport.play());




        activateBtn.setOnAction(e -> {
            String code = keyField.getText().trim();
            if (!code.isEmpty()) {
                if (db.isValidKey(code)) {
                    db.markAsActivated();
                    status.setText("✅ Activation successful! Thank you.");
                    NotificationUtil.showSystemTrayMessage("Libra Activated", "Your trial has been successfully activated.");
                } else {
                    status.setText("❌ Activation failed. Please check your code.");
                }
            } else {
                status.setText("⚠️ Please enter a valid activation code.");
            }
        });

        // Submit on Enter key
        keyField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                activateBtn.fire();
            }
        });

        HBox activationLine = new HBox(10, keyField, activateBtn);
        activationLine.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(heading, status, activationLine);


        this.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        this.getStylesheets().add(getClass().getResource("/recovery.css").toExternalForm());

        return box;
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
        resetBtn.setStyle("-fx-font-weight: bold");
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
