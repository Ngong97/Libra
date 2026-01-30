package com.ngong.librasoftware.view;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.utils.AnimationUtils;
import com.ngong.librasoftware.utils.UIUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.paint.Color;
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
        // ────────────── GLOBAL STYLING ──────────────
        this.setPadding(new Insets(30));
        this.setSpacing(30);
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: #f7f9fc;"); // soft background

        bookHoverContent.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #e0e0e0;
            -fx-border-radius: 12;
            -fx-background-radius: 12;
            -fx-padding: 14;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 4);
            -fx-font-size: 13px;
        """);
        bookHoverContent.setPrefWidth(300);
        bookHoverPopup.getContent().add(bookHoverContent);
        bookHoverPopup.setAutoHide(true);

        File file = new File(System.getProperty("user.home"), "book_inventory.txt");
        db.exportUniqueBookTitlesToFile(file);

        // ────────────── TOP BAR ──────────────
        Label greeting = new Label(getTimeGreeting() + " 👋");
        greeting.setFont(Font.font("Segoe UI Semibold", 20));
        greeting.setTextFill(Color.web("#333"));

        Label schoolLabel = new Label(db.fetchSchoolName());
        schoolLabel.setFont(Font.font("Matura MT Script Capitals", 20));
        schoolLabel.setTextFill(Color.web("#666"));

        ImageView profileImage = new ImageView(new Image(getClass().getResource("/images/user.png").toExternalForm()));
        profileImage.setFitWidth(40);
        profileImage.setFitHeight(40);
        profileImage.setPreserveRatio(true);
        profileImage.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 4, 0, 0, 1); -fx-background-radius: 50%;");

        Label usernameLabel = new Label("Ngong");
        usernameLabel.setFont(Font.font("Segoe UI", 15));

        HBox profileBox = new HBox(10, profileImage, usernameLabel);
        profileBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(20, greeting, spacer, schoolLabel);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 0, 10, 0));

        // ────────────── STATS GRID ──────────────
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(30);
        statsGrid.setVgap(25);

        statsGrid.add(createStatCard("📚 Borrowed Books", db.countOverdueBooks("", "", "", "", null, "", "")), 0, 0);
        statsGrid.add(createStatCard("🎓 Active Students", db.countActiveStudents()), 1, 0);
        statsGrid.add(createStatCard("👦 Males", db.countActiveMales()), 0, 1);
        statsGrid.add(createStatCard("👧 Females", db.countActiveFemales()), 1, 1);

        // ────────────── PROGRESS STRIP ──────────────
        int totalInventory = db.countTotalInventory();
        int currentlyBorrowed = db.countOverdueBooks("", "", "", "", null, "", "");
        double borrowRate = totalInventory > 0 ? (currentlyBorrowed * 1.0 / totalInventory) : 0.0;

        Label progressLabel = new Label("📦 Books Off Shelf: " + Math.round(borrowRate * 100) + "%");
        progressLabel.setFont(Font.font("Segoe UI Semibold", 14));

        ProgressBar progressBar = new ProgressBar(borrowRate);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setMaxHeight(12);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
//        progressBar.setPrefWidth(250);

        HBox progressRow = new HBox(20, progressLabel, progressBar);
        progressRow.setAlignment(Pos.CENTER_LEFT);
        progressRow.setPadding(new Insets(15));
        progressRow.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);
        """);

        // ────────────── TRENDS ROW ──────────────
        HBox trendsRow = new HBox(100,
                createTrendCard("📘 Most Read", db.getMostReadBookTitle()),
                createTrendCard("🚻 Top Gender", db.getMostActiveGender()),
                createTrendCard("🏫 Active Class", db.getTopReadingClass())
        );
        trendsRow.setAlignment(Pos.CENTER);

        // ────────────── CONTENT AREA (Top 5 Books + Recent Activity) ──────────────
        VBox topBooksBox = createTopBooksBox();
        VBox activityBox = createActivityBox();

        HBox contentGrid = new HBox(30, topBooksBox, activityBox);
        contentGrid.setAlignment(Pos.TOP_CENTER);

        // Equal width
        HBox.setHgrow(topBooksBox, Priority.ALWAYS);
        HBox.setHgrow(activityBox, Priority.ALWAYS);
        topBooksBox.setPrefWidth(350);
        activityBox.setPrefWidth(350);

        // ────────────── Assemble Layout ──────────────



        VBox offShelf=new VBox(40);
        offShelf.getChildren().addAll(progressRow,trendsRow);

        HBox firstBox=new HBox(150);
        firstBox.getChildren().addAll(statsGrid,offShelf);

        this.getChildren().addAll(topBar,firstBox, contentGrid);
        AnimationUtils.applyFadeIn(this, 700);
    }

    private VBox createTopBooksBox() {
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

            bookLabel.setOnMouseEntered(e -> showBookHoverPopup(e, bookTitle));
            bookLabel.setOnMouseExited(e -> bookHoverPopup.hide());

            bookList.getChildren().add(bookLabel);
        }

        topBooksBox.getChildren().addAll(topBooksLabel, bookList);
        return topBooksBox;
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

    private VBox createActivityBox() {
        VBox activityBox = new VBox(16);
        activityBox.getStyleClass().addAll("card", "highlight-panel");

        Label activityLabel = new Label("📌 Recent Activity");
        activityLabel.getStyleClass().add("section-header");

        VBox animatedFeed = new VBox(10);
        animatedFeed.setPrefHeight(220);
        animatedFeed.setPadding(new Insets(10));

        ScrollPane scroll = new ScrollPane(animatedFeed);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(220);
        scroll.getStyleClass().add("activity-scroll");

        activityBox.getChildren().addAll(activityLabel, scroll);

        // Collect Activities
        List<String> allActivities = new ArrayList<>();
        db.getRecentActivities().forEach((name, books) -> {
            if (!books.isEmpty()) {
                allActivities.add(name + " borrowed: " + String.join(", ", books));
            }
        });
        db.getRecentlyClearedStudents(10).forEach(name -> allActivities.add(name + " was cleared ✅"));

        rotatingActivities.clear();
        rotatingActivities.addAll(allActivities);
        Collections.shuffle(rotatingActivities);

        // Animate activities
        Timeline refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> showNextActivityChunk(animatedFeed)),
                new KeyFrame(Duration.seconds(8))
        );
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();

        return activityBox;
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

    // KEEP ALL YOUR EXISTING METHODS (createTopBooksBox, createActivityBox, animateCountUp, etc.)
    // unchanged — only visual polish was adjusted.
    // I’ve left them out here to avoid repetition.

    private VBox createStatCard(String label, int value) {
        Label number = new Label(String.valueOf(value));
        number.setFont(Font.font("Segoe UI Bold", 35));
        number.setTextFill(Color.web("#2c3e50"));


        Label title = new Label(label);
        title.setFont(Font.font("Segoe UI", 13));
        title.setTextFill(Color.web("#7f8c8d"));

        VBox box = new VBox(8, number, title);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
        """);
        return box;
    }

    private VBox createTrendCard(String label, String value) {
        Label val = new Label(value != null ? value : "—");
        val.setFont(Font.font("Segoe UI Semibold", 15));
        val.setTextFill(Color.web("#34495e"));

        Label tag = new Label(label);
        tag.setFont(Font.font("Segoe UI", 12));
        tag.setTextFill(Color.web("#7f8c8d"));

        VBox box = new VBox(5, val, tag);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(12));
        box.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);
        """);
        return box;
    }

    private String getTimeGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }
}
