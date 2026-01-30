package com.ngong.librasoftware.Controller;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.model.PendingBook;
import com.ngong.librasoftware.utils.TransientMessage;
import com.ngong.librasoftware.utils.TrialManager;
import com.ngong.librasoftware.utils.UIUtils;
import com.ngong.librasoftware.utils.WarningMessage;
import com.ngong.librasoftware.view.*;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DashboardApp extends Application {
    private final DatabaseService db = new DatabaseService();
    final String computer_username=System.getProperty("user.name");
    final String DB_URL = "jdbc:sqlite:C:\\Users\\"+computer_username+"\\libraDB\\libraDB.db";

    private VBox navMenu;
    private VBox sidebar;
    public StackPane contentArea;

    @Override
    public void start(Stage primaryStage) throws SQLException {
        UIUtils.applyAppIcon(primaryStage);
        contentArea = new StackPane();
        BorderPane root = new BorderPane();
        root.setCenter(contentArea);
        contentArea.getChildren().setAll(new DashboardView(contentArea));

//        db.removeClearedRecords();

        if (!db.hasFirstLaunchDate()) {
            db.setFirstLaunchDate(LocalDate.now()); // Record initial use
        }

        //logging ...........







        Button toggleBtn = new Button("☰");
        toggleBtn.getStyleClass().add("toggle-button");
        toggleBtn.setCursor(Cursor.HAND);

        // Create nav buttons with icons and hover labels
        Button dashboardBtn = createNavButton("Dashboard", "/images/dashboard.png");
        dashboardBtn.getStyleClass().add("selected");

        Button booksBtn = createNavButton("Inventory", "/images/books.png");
        Button studentsBtn = createNavButton("Overdue Records", "/images/reading.png");
        Button checkoutBtn = createNavButton("Hand book out", "/images/addstudent.png");
        Button statsBtn = createNavButton("Statistics", "/images/statistics.png");
        Button settingsBtn = createNavButton("Settings", "/images/setting.png");
        Button aboutBtn = createNavButton("About", "/images/aboutus.png");
        Button reportBtn = createNavButton("Make Report", "/images/report.png");
        Button logout=createNavButton("Logout","/images/logout.png");
        List<Button> navButtons = List.of(dashboardBtn, booksBtn, studentsBtn,checkoutBtn, statsBtn, settingsBtn, aboutBtn,reportBtn,logout);

        // Assign actions
        reportBtn.setOnAction(e -> {
            if (isTrialExpired()) {
                promptForActivationKey();
                return;
            }
            contentArea.getChildren().setAll(new ReportView());
            setSelectedNav(reportBtn, navButtons);
        });

        dashboardBtn.setOnAction(e -> {
            contentArea.getChildren().setAll(new DashboardView(contentArea));
            setSelectedNav(dashboardBtn, navButtons);
        });

        booksBtn.setOnAction(e -> {
            if (isTrialExpired()) {
                promptForActivationKey();
                return;
            }
            contentArea.getChildren().setAll(new BooksView(contentArea));
            setSelectedNav(booksBtn, navButtons);
        });

        studentsBtn.setOnAction(e -> {
            if (isTrialExpired()) {
                promptForActivationKey();
                return;
            }
            contentArea.getChildren().setAll(new StudentsView(contentArea));
            setSelectedNav(studentsBtn, navButtons);
        });


        checkoutBtn.setOnAction(e -> {
            if (isTrialExpired()) {
                promptForActivationKey();
                return;
            }
            contentArea.getChildren().setAll(new CheckOutView(contentArea));
            setSelectedNav(checkoutBtn, navButtons);
        });


        statsBtn.setOnAction(e -> {
            if (isTrialExpired()) {
                promptForActivationKey();
                return;
            }
            contentArea.getChildren().setAll(new StatisticsView());
            setSelectedNav(statsBtn, navButtons);
        });

        settingsBtn.setOnAction(e -> {
            if (isTrialExpired()) {
                promptForActivationKey();
                return;
            }

            // Create a custom dialog
            Dialog<String> passwordDialog = new Dialog<>();
            passwordDialog.setTitle("UI lock password");
            passwordDialog.setHeaderText(null);
            passwordDialog.setGraphic(null);
            passwordDialog.initOwner(primaryStage);

// Create the password field
            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("••••••••");

// Create the label
            Label promptLabel = new Label("Please type the UI lock password:");
            promptLabel.setAlignment(Pos.CENTER);

// Create OK and Cancel buttons
            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            passwordDialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

// Layout: vertical stack with centered alignment
            VBox contentBox = new VBox(5);
            contentBox.setAlignment(Pos.CENTER);
            contentBox.getChildren().addAll(promptLabel, passwordField);

// Replace default content with custom layout
            passwordDialog.getDialogPane().setContent(contentBox);


// Apply layout to dialog
            DialogPane dialogPane = passwordDialog.getDialogPane();
            dialogPane.setContent(contentBox);

// 🎯 Apply rounded corners and styling
            dialogPane.setStyle("""
    -fx-background-color: #f4f4f4;
    -fx-border-radius: 15;
    -fx-background-radius: 12;
    -fx-padding: 10;
""");



// Center the buttons manually
            Node okButton = passwordDialog.getDialogPane().lookupButton(okButtonType);
            Node cancelButton = passwordDialog.getDialogPane().lookupButton(cancelButtonType);
            HBox buttonBox = new HBox(20, okButton, cancelButton);
            buttonBox.setAlignment(Pos.CENTER);
            contentBox.getChildren().add(buttonBox);

// Result converter to capture input
            passwordDialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return passwordField.getText().trim();
                }
                return null;
            });

// Show dialog and handle result
            Optional<String> passwordInput = passwordDialog.showAndWait();
            if (passwordInput.isEmpty()) return;

            String password = passwordInput.get();

// Step 2: Verify password via your DatabaseService
            if (db.isValidPassword(password)) {
                contentArea.getChildren().setAll(new SettingsView());
                setSelectedNav(settingsBtn, navButtons);
            } else {
                WarningMessage msg = new WarningMessage(
                        "Incorrect password. Access denied.",
                        Duration.seconds(3),
                        contentArea
                );
                contentArea.getChildren().add(msg);
            }




        });
        aboutBtn.setOnAction(e -> {
            contentArea.getChildren().setAll(new AboutView());
            setSelectedNav(aboutBtn, navButtons);
        });

        logout.setOnAction(e -> {
            // Create custom buttons
            ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);

            // Create the confirmation alert
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to logout?",
                    yesButton, noButton);
            alert.setTitle("Exit?");
            alert.setHeaderText(null); // removes default header
            alert.initOwner(primaryStage);

            // Show and wait for user response
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == yesButton) {
                primaryStage.close();
                Stage registerstage=new Stage();
                LoginWindow loginWindow = new LoginWindow();
                loginWindow.start(registerstage);
            }
        });

        setSelectedNav(dashboardBtn, navButtons); // ensures CSS class is synced

        DropShadow blueShadow = new DropShadow();
        blueShadow.setOffsetY(2.0);
        blueShadow.setColor(Color.BLUE); // Set shadow color and transparency



        for (Button button : navButtons) {
            button.setOnMouseEntered(event -> {
                button.setEffect(blueShadow);
            });
            button.setOnMouseExited(event -> {
                button.setEffect(null);
            });
        }




        // Sidebar layout
        navMenu = new VBox(10, dashboardBtn, booksBtn, studentsBtn, checkoutBtn,statsBtn, settingsBtn,reportBtn, aboutBtn,new Label(),logout);
        navMenu.setVisible(true);
        navMenu.setManaged(true);

        sidebar = new VBox(10, toggleBtn, navMenu);
        sidebar.setPrefWidth(130);
        sidebar.setMinWidth(50);
        sidebar.setId("sidebar");

        toggleBtn.setOnAction(e -> {
            boolean isCollapsed = sidebar.getPrefWidth() > 50;
            sidebar.setPrefWidth(isCollapsed ? 50 : 130);
            if (isCollapsed) {
                fadeOut(navMenu, Duration.millis(300));
            } else {
                fadeIn(navMenu, Duration.millis(300));
            }
        });

        root.setLeft(sidebar);
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Libra");
        primaryStage.show();

        Platform.runLater(() -> {
            List<PendingBook> pendingBooks = db.getPendingBooks();

            if (!pendingBooks.isEmpty()) {
                if (db.isConnectedToInternet()) {
                    syncPendingDescriptions(); // Background metadata recovery
                } else {
                    String hint = buildMissingListSummary(pendingBooks);

                    TransientMessage msg = new TransientMessage(
                            hint,
                            Duration.seconds(5),
                            contentArea // Your layout container
                    );

                    contentArea.getChildren().add(msg);
                    StackPane.setAlignment(msg, Pos.CENTER);
                }
            }
        });


        if (!db.isActivated()) {
            TrialManager.scheduleTrialNotifications();
        }


    }

    private void promptForActivationKey() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Activation Required");
        dialog.setHeaderText("Your trial has ended");
        dialog.setContentText("Enter your activation key:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(key -> {
            if (db.isValidKey(key)) {
                db.markAsActivated();  // Update DB flag
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                alert.setTitle("Success!");
                alert.setContentText("Activation successful!");
                alert.showAndWait();
//                showSuccessMessage();  // Optional transient UI feedback
            } else {
//                showSuccessMessage();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                alert.setTitle("Activation Required");
                alert.setHeaderText("Activation Required");
                alert.setContentText("Activation failed.");
                alert.showAndWait();

            }
        });
    }

    public static void showSuccessMessage(String message, Window contextWindow) {
        Label label = new Label(message);
        label.setStyle("""
        -fx-background-color: #4CAF50;
        -fx-text-fill: white;
        -fx-padding: 10;
        -fx-font-weight: bold;
        -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 6, 0, 0, 2);
    """);

        Popup popup = new Popup();
        popup.getContent().add(label);
        popup.setAutoHide(true);

        // Float bottom-right with margin
        double marginX = 20;
        double marginY = 40;
        double x = contextWindow.getX() + contextWindow.getWidth() - label.prefWidth(-1) - marginX;
        double y = contextWindow.getY() + contextWindow.getHeight() - label.prefHeight(-1) - marginY;

        popup.show(contextWindow, x, y);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> popup.hide());
        delay.play();
    }


    private boolean isTrialExpired() {
        LocalDate launchDate = db.getFirstLaunchDate();
        LocalDate today = LocalDate.now();
        return today.isAfter(launchDate.plusDays(60)) && !db.isActivated();
    }






    public String buildMissingListSummary(List<PendingBook> books) {
        StringBuilder sb = new StringBuilder("📡 Connect to internet to load descriptions:\n");
        int max = Math.min(3, books.size());

        int count=0;
        for (int i = 0; i < max; i++) {
            count++;
            sb.append(count+". ")
                    .append(books.get(i).title())
                    .append(" — ")
                    .append(books.get(i).author())
                    .append("\n");
        }

        if (books.size() > max) {
            sb.append("...and ").append(books.size() - max).append(" more.");
        }

        return sb.toString().trim();
    }


    public void syncPendingDescriptions() {
        if (!db.isConnectedToInternet()) return;

        HBox loader = showSyncLoader("Syncing book descriptions...");

        Task<Void> syncTask = new Task<>() {
            @Override
            protected Void call() {
                List<PendingBook> pendingBooks = db.getPendingBooks();

                for (PendingBook book : pendingBooks) {
                    String desc = db.fetchBookDescription(book.title(), book.author());
                    boolean hasValidDesc = desc != null && !desc.isBlank() && !desc.equals("No description found");

                    if (hasValidDesc) {
                        db.updateBookDescription(book.title(), book.author(), desc);
                        db.removeFromPending(book.title(), book.author());
                    } else {
                        db.incrementRetryCount(book.title(), book.author());
                        if (book.retryCount() >= 2) {
                            db.updateBookDescription(book.title(), book.author(), "No description found");
                            db.removeFromPending(book.title(), book.author());
                        }
                    }
                }
                return null;
            }

            @Override
            protected void succeeded() {
                contentArea.getChildren().remove(loader);
                TransientMessage doneMsg = new TransientMessage("✅ Book descriptions synced.", Duration.seconds(3), contentArea);
                contentArea.getChildren().add(doneMsg);
                StackPane.setAlignment(doneMsg, Pos.CENTER);
            }
        };

        new Thread(syncTask).start();
    }



    private HBox showSyncLoader(String message) {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(70, 70);

        Label label = new Label(message);
        label.getStyleClass().add("sync-label");

        HBox loaderBox = new HBox(8, spinner, label);
        loaderBox.setAlignment(Pos.CENTER);
        loaderBox.setStyle("-fx-background-color: rgba(0,0,0,0.07); -fx-padding: 6 12; -fx-background-radius: 8;");
        loaderBox.setId("sync-loader");

        StackPane.setAlignment(loaderBox, Pos.TOP_CENTER);
        contentArea.getChildren().add(loaderBox);

        return loaderBox;
    }

    private Button createNavButton(String labelText, String iconPath) {
        ImageView icon = new ImageView(new Image(getClass().getResource(iconPath).toExternalForm()));
        icon.setFitWidth(35);
        icon.setFitHeight(35);

        Button button = new Button();
        button.setGraphic(icon);
        button.setStyle("-fx-background-color: transparent;");
        button.setCursor(Cursor.HAND);
        button.getStyleClass().add("nav-button");




        Tooltip tooltip = new Tooltip(labelText);
        tooltip.getStyleClass().add("nav-tooltip");
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setHideDelay(Duration.millis(100));
        Tooltip.install(button, tooltip);

        return button;
    }


    private void setSelectedNav(Button selected, List<Button> allButtons) {
        allButtons.forEach(btn -> btn.getStyleClass().remove("selected"));
        selected.getStyleClass().add("selected");
    }

    private void fadeIn(Node node, Duration duration) {
        node.setVisible(true);
        node.setManaged(true);
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void fadeOut(Node node, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            node.setVisible(false);
            node.setManaged(false);
        });
        fade.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
