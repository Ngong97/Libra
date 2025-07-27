
package com.ngong.librasoftware.view;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.utils.AnimationUtils;
import com.ngong.librasoftware.utils.UIUtils;
import javafx.animation.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.io.File;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardView extends VBox {
    private final DatabaseService db = new DatabaseService();
    private final Popup bookHoverPopup = new Popup();
    private final VBox bookHoverContent = new VBox(8);

    private List<String> rotatingActivities = new ArrayList<>();
    private int activityIndex = 0;

    public DashboardView(Pane contentArea) {
        bookHoverContent.setStyle("""
        -fx-background-color: white;
        -fx-border-color: #ccc;
        -fx-border-radius: 10;
        -fx-background-radius: 10;
        -fx-padding: 12;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);
        -fx-font-size: 12px;
    """);
        bookHoverContent.setPrefWidth(280);
        bookHoverPopup.getContent().add(bookHoverContent);
        bookHoverPopup.setAutoHide(true);


        File file = new File(System.getProperty("user.home"), "book_inventory.txt");
        db.exportUniqueBookTitlesToFile(file);
//        this.setPadding(new Insets(0, 0, 0, 30));  // adds 20px padding on the left side, pushing all content right

        this.setPadding(new Insets(30));
        this.setSpacing(25);
        this.getStyleClass().add("dashboard");


        // 1. Greeting
        Label greeting = new Label(getTimeGreeting() + " 👋");
        greeting.setFont(Font.font(20));
        greeting.getStyleClass().add("dashboard-greeting");

        // 2. Live Stat Cards
        HBox statCards = new HBox(50);
//        statCards.getStyleClass().add("section-box");
        statCards.setAlignment(Pos.CENTER_LEFT);
        statCards.getChildren().addAll(
                createStatCard("📚 Borrowed Books", db.countOverdueBooks("","","","",null,"","")),
                createStatCard("🎓 Active Students", db.countActiveStudents()),
                createStatCard("📦 Males", db.countActiveMales()),
                createStatCard("⚠️ Females", db.countActiveFemales())
        );



        int totalInventory = db.countTotalInventory(); // sum of total_quantity
        int currentlyBorrowed = db.countOverdueBooks("","","","",null,"",""); // borrowings where returned = 0

        double borrowRate = totalInventory > 0 ? (currentlyBorrowed * 1.0 / totalInventory) : 0.0;

        Label progressLabel = new Label("📦 Books Off shelf: " + Math.round(borrowRate * 100) + "%");
        progressLabel.getStyleClass().add("progress-label");

        ProgressBar progressBar = new ProgressBar(borrowRate);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progressBar, Priority.ALWAYS);


        HBox progressRow = new HBox(10, progressLabel, progressBar);
        progressRow.setAlignment(Pos.CENTER_LEFT);
        progressRow.setPadding(new Insets(10));
        progressRow.setMaxWidth(835);

        VBox returnBox = new VBox(progressRow);
        returnBox.setAlignment(Pos.CENTER_LEFT);
        returnBox.setMaxWidth(Double.MAX_VALUE);

        // 4. Trends
        HBox trends = new HBox(20,
                createTrendCard("📘 Most Read", db.getMostReadBookTitle()),
                createTrendCard("🚻 Top Gender", db.getMostActiveGender()),
                createTrendCard("🏫 Active Class", db.getTopReadingClass())
        );
        trends.setSpacing(250);
        trends.getStyleClass().add("trend-section");
        trends.setAlignment(Pos.CENTER_LEFT);
//        trends.getStyleClass().add("section-box");

        VBox topBooksBox = new VBox(10);
        topBooksBox.getStyleClass().addAll("card", "top-books-box");

        Label topBooksLabel = new Label("📘 Top 5 Borrowed Books");
        topBooksLabel.getStyleClass().add("section-header");

        VBox bookList = new VBox(8);
        List<String> topBooks = db.getTopBorrowedBooks();

        for (int i = 0; i < topBooks.size(); i++) {
            String bookTitle = topBooks.get(i);
            Label bookLabel = new Label((i + 1) + ". " + bookTitle);
            bookLabel.getStyleClass().add("activity-item");

            // Hover behavior
            bookLabel.setOnMouseEntered(e -> showBookHoverPopup(e, bookTitle));
            bookLabel.setOnMouseExited(e -> bookHoverPopup.hide());

            bookList.getChildren().add(bookLabel);
        }


        topBooksBox.getChildren().addAll(topBooksLabel, bookList);

// 6. Recent Activity (Bullet Points with Time Feel)
        VBox activityBox = new VBox(16);
        activityBox.setMinWidth(450);
        activityBox.getStyleClass().addAll("card", "highlight-panel");

        Label activityLabel = new Label("📌 Recent Activity");
        activityLabel.getStyleClass().add("section-header");

        VBox animatedFeed = new VBox(10);
        animatedFeed.setPrefHeight(220);
        animatedFeed.setPadding(new Insets(10));

        ScrollPane scroll = new ScrollPane(animatedFeed);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(220);
//        scroll.setPrefWidth(350);
        scroll.getStyleClass().add("activity-scroll");

        activityBox.getChildren().addAll(activityLabel, scroll);

// 🔄 Combine borrowing and clearing activities
        List<String> allActivities = new ArrayList<>();

        db.getRecentActivities().forEach((name, books) -> {
            if (!books.isEmpty()) {
                allActivities.add(name + " borrowed: " + String.join(", ", books));
            }
        });

        List<String> cleared = db.getRecentlyClearedStudents(10);
        for (String name : cleared) {
            allActivities.add(name + " was cleared ✅");
        }

// 🔁 Animate and rotate every 8 seconds
        // Initialize and shuffle once
        rotatingActivities.clear();
        rotatingActivities.addAll(allActivities);
        Collections.shuffle(rotatingActivities);

// Start rotating
        Timeline refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> showNextActivityChunk(animatedFeed)),
                new KeyFrame(Duration.seconds(8))
        );
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();



// User profile image
        ImageView profileImage = new ImageView(new Image(getClass().getResource("/images/user.png").toExternalForm()));
        profileImage.setFitWidth(32);
        profileImage.setFitHeight(32);
//        ImageView profileImage = UIUtils.loadExternalImageView("/images/search.png",32,32);

        profileImage.setPreserveRatio(true);
        profileImage.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);");

// Username label (fetched from settings)
        Label usernameLabel = new Label("Ngong");
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-padding: 0 8 0 4;");

// Combine icon + name
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);


        String schoolName = db.fetchSchoolName();

        Label schoolLabel = new Label(schoolName);
        schoolLabel.getStyleClass().add("school-label");

        HBox topBar = new HBox(20, greeting,schoolLabel);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);

// 7. Combine as balanced cards
        HBox bottomRow = new HBox(40, topBooksBox, activityBox);
//        bottomRow.getStyleClass().add("section-box");
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.setPadding(new Insets(10, 0, 0, 0));

        VBox.setMargin(topBar, new Insets(0, 0, 0, 85));  // push this child 30px right by setting left margin
        VBox.setMargin(statCards, new Insets(0, 0, 0, 90));  // push this child 30px right by setting left margin
        VBox.setMargin(returnBox, new Insets(0, 0, 0, 90));  // push this child 30px right by setting left margin
        VBox.setMargin(trends, new Insets(0, 0, 0, 90));  // push this child 30px right by setting left margin
        VBox.setMargin(bottomRow, new Insets(0, 0, 0, 90));  // push this child 30px right by setting left margin



        this.getChildren().addAll(topBar,returnBox,statCards, trends, bottomRow);;

        // Smooth blink for stat cards every 5 seconds
        Timeline cardBlink = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            statCards.getChildren().forEach(node -> {
                if (node instanceof VBox card) {
                    FadeTransition fade = new FadeTransition(Duration.seconds(1.2), card);
                    fade.setFromValue(1.0);
                    fade.setToValue(0.6);
                    fade.setAutoReverse(true);
                    fade.setCycleCount(2);
                    fade.play();
                }
            });
        }));
        cardBlink.setCycleCount(Animation.INDEFINITE);
        cardBlink.play();

// 🔢 Animate stat numbers from 0 to value every 10 seconds
        Timeline countUpTimeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
            statCards.getChildren().forEach(node -> {
                if (node instanceof VBox card) {
                    card.getChildren().stream()
                            .filter(child -> child instanceof Label && "stat-number".equals(child.getId()))
                            .forEach(label -> {
                                int target = (int) ((Label) label).getUserData();
                                animateCountUp((Label) label, target, Duration.seconds(1.5));
                            });
                }
            });
        }));
        countUpTimeline.setCycleCount(Animation.INDEFINITE);
        countUpTimeline.play();

        this.setAlignment(Pos.TOP_CENTER);


        //        VBox.setVgrow(overlayPane, Priority.ALWAYS);
        // Fade animation
        AnimationUtils.applyFadeIn(this, 600);





    }

private void animateCountUp(Label label, int target, Duration duration) {
    IntegerProperty value = new SimpleIntegerProperty(0);
    label.textProperty().bind(value.asString());

    Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(value, 0)),
            new KeyFrame(duration, new KeyValue(value, target))
    );

    timeline.setOnFinished(e -> {
        label.textProperty().unbind();
        label.setText(String.valueOf(target)); // ensure final value is correct
    });

    timeline.play();
}


private void showBookHoverPopup(MouseEvent event, String bookTitle) {
    bookHoverContent.getChildren().clear();

    // 🔍 Fetch book details
    String author = db.getAuthorForBookFromBookDictionary(bookTitle);
    String description = db.getShortDescription(bookTitle);
    String imagePath = db.getCoverImagePath(bookTitle);

    Image coverImage;
    try {
        coverImage = UIUtils.loadExternalImage(imagePath);
    } catch (Exception e) {
        coverImage = UIUtils.loadExternalImage("/images/default_cover.png");
    }

    // 🖼 Cover image
    ImageView cover = new ImageView(coverImage);
    cover.setFitWidth(60);
    cover.setFitHeight(90);
    cover.setPreserveRatio(true);

    // 📚 Book info
    Label titleLabel = new Label(bookTitle);
    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

    Label authorLabel = new Label("By " + author);
    authorLabel.setStyle("-fx-text-fill: #555;");

    Label descLabel = new Label(description != null ? description : "No description available.");
    descLabel.setWrapText(true);
    descLabel.setMaxWidth(200);

    VBox left = new VBox(cover);
    VBox right = new VBox(4, titleLabel, authorLabel, descLabel);
    HBox header = new HBox(10, left, right);

    // 👥 Borrowers section
    List<String> borrowers = db.getRecentBorrowersForBook(bookTitle);
    VBox borrowerList = new VBox(3);
    for (String name : borrowers) {
        Label borrower = new Label("• " + name);
        borrower.setStyle("-fx-text-fill: #444;");
        borrowerList.getChildren().add(borrower);
    }

    Label borrowedBy = new Label("Borrowed by:");
    borrowedBy.setStyle("-fx-font-weight: bold; -fx-padding: 6 0 0 0;");

    // 🧩 Assemble all content
    bookHoverContent.getChildren().addAll(header, borrowedBy, borrowerList);

    // 📍 Position: bottom-left corner of popup aligned to top-right of pointer
//    double mouseX = event.getScreenX();
//    double mouseY = event.getScreenY();

    double offsetX = 15;
    double offsetY = 10;
    double popupHeight = bookHoverContent.prefHeight(-1);

    double popupX = event.getScreenX() + offsetX;
    double popupY = event.getScreenY() - popupHeight - offsetY;

    bookHoverPopup.show((Node) event.getSource(), popupX, popupY);
}


    private void animateLine(Label label, String text, int delayMs, Runnable onFinished) {
    label.setText("");
    Timeline timeline = new Timeline();

    for (int i = 0; i < text.length(); i++) {
        final int index = i;
        KeyFrame frame = new KeyFrame(Duration.millis(delayMs * i), e -> {
            label.setText(text.substring(0, index + 1));
        });
        timeline.getKeyFrames().add(frame);
    }

    timeline.setOnFinished(e -> {
        if (onFinished != null) onFinished.run();
    });

    timeline.play();
}


    private void showNextActivityChunk(VBox feedBox) {
        feedBox.getChildren().clear();

        int end = Math.min(activityIndex + 5, rotatingActivities.size());
        List<String> subset = rotatingActivities.subList(activityIndex, end);

        activityIndex += 5;
        if (activityIndex >= rotatingActivities.size()) {
            activityIndex = 0;
            Collections.shuffle(rotatingActivities);
        }

        animateLinesSequentially(feedBox, subset, 0);
    }

    private void animateLinesSequentially(VBox feedBox, List<String> lines, int index) {
        if (index >= lines.size()) return;

        String activity = lines.get(index);
        Label line = new Label();
        line.getStyleClass().add("activity-item");
        line.setWrapText(true);
        line.setMaxWidth(400);

        if (activity.contains("was cleared")) {
            line.setStyle("-fx-text-fill: green;");
        }

        feedBox.getChildren().add(line);

        animateLine(line, "• " + activity, 30, () -> {
            animateLinesSequentially(feedBox, lines, index + 1);
        });
    }

    private VBox createStatCard(String label, int value) {
        Label number = new Label(String.valueOf(value)); // show actual value immediately
        number.getStyleClass().add("stat-value");
        number.setId("stat-number");
        number.setUserData(value); // store the target value for future animations

        Label title = new Label(label);
        title.getStyleClass().add("stat-label");

        VBox box = new VBox(4, title,number);
        box.getStyleClass().add("stat-card");
        box.setAlignment(Pos.CENTER);
        return box;
    }


    private VBox createTrendCard(String label, String value) {
        Label tag = new Label(label);
        tag.getStyleClass().add("trend-label");

        Label val = new Label(value != null ? value : "—");
        val.getStyleClass().add("trend-value");

        VBox box = new VBox(5, tag, val);
        box.getStyleClass().add("trend-card");
        return box;
    }


    private String getTimeGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour > 12 && hour < 17) return "Good afternoon";
        return "Good evening";
    }
}
