
package com.ngong.librasoftware.view;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.utils.AnimationUtils;
import com.ngong.librasoftware.utils.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.net.URI;
import java.time.Year;

public class AboutView extends BorderPane {
    private final Popup bookHoverPopup = new Popup();
    private final VBox bookHoverContent = new VBox(8);
    private final DatabaseService db = new DatabaseService();

    private int slideIndex = 0;

    public AboutView() {
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




        this.setPadding(new Insets(20));
        this.getStyleClass().add("about-root");

        Label heading = new Label("📘 About Libra");
        heading.getStyleClass().add("about-heading");

        Label developer=createWrappedLabel("• Developer: Abraham N. Manyuon");
        developer.setStyle("-fx-font-weight: bold");
        developer.setOnMouseEntered(e -> showDeveloperPopup(e, "ngong"));
        developer.setOnMouseExited(e -> bookHoverPopup.hide());
        developer.setCursor(Cursor.HAND);

        Hyperlink email=new Hyperlink("Email: joncopy7@gmail.com");
        email.getStyleClass().add("hyper-label");
        email.setOnMouseClicked(e -> {
            try {
                URI uri = new URI("https://mail.google.com/mail/?view=cm&fs=1&to=joncopy7@gmail.com");
                java.awt.Desktop.getDesktop().browse(uri);
            } catch (Exception ex) {
                ex.printStackTrace(); // Handle gracefully if needed
            }
        });
        developer.setCursor(Cursor.HAND);

        Label acadBackground=createWrappedLabel("• Academic Background:");
        acadBackground.setStyle("-fx-font-weight: bold");


        VBox devInfo = new VBox(
                createSectionTitle("👨‍💻 Developer Info"),
                developer,
                email,
                createWrappedLabel("• Location: Juba, South Sudan"),
                createWrappedLabel("• Project Started: December 2023"),
                createWrappedLabel("• Project Finished: July 2025"),
                acadBackground,
                createWrappedLabel("2020: Advanced Certificate in Computer Studies, St. Vincent De Paul VTC, Lologo II - Juba, South Sudan."),
                createWrappedLabel("2021–2023: Diploma in Information Technology, Don Bosco Vocational Training Centre, Gumbo-Juba, South Sudan.")
        );
        devInfo.getStyleClass().add("section-box");
//        devInfo.setCursor(Cursor.HAND);
        devInfo.setSpacing(10);
        VBox purpose = new VBox(
                createSectionTitle("🎯 Purpose"),
                createWrappedLabel("Libra simplifies and digitizes book borrowing for school librarians. It tracks checkouts, flags overdue returns, and allows for real-time student clearance status—cutting back paperwork and boosting accountability.")
        );
        purpose.getStyleClass().add("section-box");


        Label libman=createWrappedLabel("📚 Library Management");
        libman.setStyle("-fx-font-weight: bold");
        libman.setCursor(Cursor.HAND);
        libman.setOnMouseEntered(e -> showBookHoverPopup(e, "Library Management"));
        libman.setOnMouseExited(e -> bookHoverPopup.hide());
        Label libmanVals=createWrappedLabel("– Koha, Destiny.");

        Label studInfo=createWrappedLabel("📝 Student Information System");
        studInfo.setStyle("-fx-font-weight: bold");
        studInfo.setOnMouseEntered(e -> showBookHoverPopup(e, "Student Information System"));
        studInfo.setOnMouseExited(e -> bookHoverPopup.hide());
        studInfo.setCursor(Cursor.HAND);
        Label studInfoVals=createWrappedLabel("– PowerSchool and OpenSIS");

        Label clearance=createWrappedLabel("🧾 Clearance & Exit Management");
        clearance.setStyle("-fx-font-weight: bold");
        clearance.setOnMouseEntered(e -> showBookHoverPopup(e, "Clearance & Exit Management"));
        clearance.setOnMouseExited(e -> bookHoverPopup.hide());
        clearance.setCursor(Cursor.HAND);

        Label acadAnalytics=createWrappedLabel("📊 Academic Analytics");
        acadAnalytics.setStyle("-fx-font-weight: bold");
        Label acadAnalVals=createWrappedLabel("– Tableau, Microsoft Power BI");
        acadAnalytics.setOnMouseEntered(e -> showBookHoverPopup(e, "Academic Analytics"));
        acadAnalytics.setOnMouseExited(e -> bookHoverPopup.hide());
        acadAnalytics.setCursor(Cursor.HAND);

        Label teacherAndClassroom=createWrappedLabel("🧑‍🏫 Teacher & Classroom Tools");
        teacherAndClassroom.setStyle("-fx-font-weight: bold");
        Label classroomTools=createWrappedLabel("– Google Classroom, ClassDojo");
        teacherAndClassroom.setOnMouseEntered(e -> showBookHoverPopup(e, "Teacher & Classroom Tools"));
        teacherAndClassroom.setOnMouseExited(e -> bookHoverPopup.hide());
        teacherAndClassroom.setCursor(Cursor.HAND);

        VBox categories = new VBox(
                createSectionTitle("🗂️ Software Categories"),
                createWrappedLabel("Libra fits within educational administration tools, streamlining school operations with enhanced accountability.Here are some real-world examples of related software categories:"),
                libman,
                libmanVals,
                studInfo,
                studInfoVals,
                clearance,
                acadAnalytics,
                acadAnalVals,
                teacherAndClassroom,
                classroomTools
        );
        categories.getStyleClass().add("section-box");
        categories.setSpacing(10);
        VBox techStack = new VBox(
                createSectionTitle("⚙️ Tech Stack Highlights"),
                createWrappedLabel("• 🎨 JavaFX – Modern UI toolkit with smooth animations"),
                createWrappedLabel("• 💅 CSS Styling – Clean separation between logic and style"),
                createWrappedLabel("• 🗃️ SQLite – Lightweight embedded database"),
                createWrappedLabel("• ☁️ Modular Architecture – Maintainable and extendable"),
                createWrappedLabel("• 🔄 Real-Time UI Updates – Observable bindings"),
                createWrappedLabel("• 🧪 Utility Classes – Reusable animations, transitions"),
                createWrappedLabel("• 🧩 Extensibility – Designed for future cloud integration")
        );
        techStack.getStyleClass().add("section-box");
        techStack.setSpacing(10);



        Label footer = new Label(
                "Libra v2.0 – © " + Year.now().getValue() + " Abraham N. Manyuon. All rights reserved.\n" +
                        "Email: joncopy7@gmail.com"
        );
        footer.getStyleClass().add("about-footer");

        Hyperlink documentation=new Hyperlink("Documentation");
        documentation.setOnAction(e -> DocumentationViewer.launchDocumentationWindow());
        VBox content = new VBox(30, heading, devInfo, purpose, categories, techStack,documentation, footer);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(30));
        content.setPrefWidth(800);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.getStyleClass().add("about-scroll");

        this.setCenter(scrollPane);
        AnimationUtils.applyFadeIn(this, 600);
    }

    private void showBookHoverPopup(MouseEvent event, String category) {
        bookHoverContent.getChildren().clear();

        // 🔍 Fetch book details
        String description = db.getDescriptionForCategory(category);
        String imagePath = db.getCoverImagePath(category);

        Image coverImage;
        try {
            coverImage = UIUtils.loadExternalImage(imagePath);
        } catch (Exception e) {
            coverImage =UIUtils.loadExternalImage("/images/default_cover.png");
        }

        // 🖼 Cover image
        ImageView cover = new ImageView(coverImage);
        cover.setFitWidth(60);
        cover.setFitHeight(90);
        cover.setPreserveRatio(true);


        Label descLabel = new Label(description != null ? description : "No description available.");
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 12");
        descLabel.setMaxWidth(200);

        VBox left = new VBox(cover);
        VBox right = new VBox(4, descLabel);
        HBox header = new HBox(10, left, right);


        // 🧩 Assemble all content
        bookHoverContent.getChildren().addAll(header);

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

    private void showDeveloperPopup(MouseEvent event, String category) {
        bookHoverContent.getChildren().clear();

        // 🔍 Fetch book details
        String description = db.getDescriptionForDeveloper(category);
        String imagePath = db.getCoverImagePathForDeveloper(category);

        Image coverImage;
        try {
            coverImage = UIUtils.loadExternalImage(imagePath);
        } catch (Exception e) {
            coverImage = UIUtils.loadExternalImage("/images/default_cover.png");
        }

        // 🖼 Cover image
        ImageView cover = new ImageView(coverImage);
        cover.setFitWidth(90);
        cover.setFitHeight(120);
        cover.setPreserveRatio(true);


        Label descLabel = new Label(description != null ? description : "No description available.");
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 12");
        descLabel.setMaxWidth(800);


        VBox left = new VBox(cover);
        VBox right = new VBox(4, descLabel);
        HBox header = new HBox(10, left, right);


        // 🧩 Assemble all content
        bookHoverContent.getChildren().addAll(header);

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




    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    private Label createWrappedLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(1000);
        label.getStyleClass().add("section-text");
        return label;
    }
}
