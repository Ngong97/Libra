//package com.ngong.librasoftware.view;
//
//
//import com.ngong.librasoftware.utils.AnimationUtils;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.ScrollPane;
//import javafx.scene.control.Tooltip;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.scene.text.Font;
//
//import java.util.List;
//
//public class AboutView extends BorderPane {
//
//    private int slideIndex = 0;
//
//    public AboutView() {
//        this.setPadding(new Insets(20));
//
//        // Heading
//        Label heading = new Label("📘 About Librasoftware");
//        heading.setFont(Font.font(22));
//        heading.setStyle("-fx-font-weight: bold;");
//
//        // Developer Info
//        Label who = new Label("👨‍💻 Developed by: Abraham N. Manyuon\n📍 Location: Juba, South Sudan\n🕒 Project Started: 2025");
//
//        // Purpose
//        Label purpose = new Label("🎯 Purpose:\nLibrasoftware simplifies and digitizes book borrowing for school librarians. It tracks checkouts, flags overdue returns, and allows for real-time student clearance status—cutting back paperwork and boosting accountability.");
//        purpose.setWrapText(true);
//
//        // Categories
//        Label categories = new Label("🗂️ Related Software Categories:\nThis tool falls within educational administration software. Similar systems—like grading apps, student trackers, and attendance managers—reduce clerical burden, improve data access, and keep school operations transparent.");
//        categories.setWrapText(true);
//
//        // Extras
//        Label extras = new Label("📌 Tech Stack Highlights:\n• Built with JavaFX for a modern interface\n• Styled via CSS with clean layout control\n• Uses SQLite for local lightweight storage\n• Modular, real-time features for librarians and admins alike");
//        extras.setWrapText(true);
//
//        // 🖼️ Slideshow with Captions
//        List<Image> images = List.of(
//                new Image(getClass().getResource("/images/dev1.jpg").toString()),
//                new Image(getClass().getResource("/images/dev2.jpg").toString()),
//                new Image(getClass().getResource("/images/dev3.jpg").toString())
//        );
//
//        List<String> captions = List.of(
//                "Ngong Chol Deng - Lead Developer & Designer",
//                "Samuel K. Ajang - Backend & Data Model",
//                "Achan M. Nyanut - UI/UX Consultant"
//        );
//
//        Label captionLabel = new Label(captions.get(0));
//        captionLabel.setStyle("-fx-font-size: 13px; -fx-font-style: italic;");
//
//        ImageView imageView = new ImageView(images.get(0));
//        imageView.setFitWidth(600);
//        imageView.setFitHeight(200);
//        imageView.setPreserveRatio(true);
//        Tooltip.install(imageView, new Tooltip(captions.get(0)));
//
//        Button left = new Button("⟨");
//        Button right = new Button("⟩");
//
//        left.setOnAction(e -> {
//            slideIndex = (slideIndex - 1 + images.size()) % images.size();
//            imageView.setImage(images.get(slideIndex));
//            captionLabel.setText(captions.get(slideIndex));
//            Tooltip.install(imageView, new Tooltip(captions.get(slideIndex)));
//        });
//
//        right.setOnAction(e -> {
//            slideIndex = (slideIndex + 1) % images.size();
//            imageView.setImage(images.get(slideIndex));
//            captionLabel.setText(captions.get(slideIndex));
//            Tooltip.install(imageView, new Tooltip(captions.get(slideIndex)));
//        });
//
//        HBox slideshowControls = new HBox(10, left, imageView, right);
//        slideshowControls.setAlignment(Pos.CENTER);
//
//        VBox slideshow = new VBox(8, new Label("📸 Meet the Developers"), slideshowControls, captionLabel);
//        slideshow.setAlignment(Pos.CENTER);
//
//        // Scrollable Info Block
//        VBox content = new VBox(20, heading, who, purpose, categories, extras);
//        content.setPadding(new Insets(10));
//        content.setPrefWidth(600);
//        ScrollPane scrollPane = new ScrollPane(content);
//        scrollPane.setFitToWidth(true);
//        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//        scrollPane.setStyle("-fx-background-color: transparent;");
//
//        // 📎 Footer
//        Label footer = new Label("Librasoftware v1.0 – Copyright © " + java.time.Year.now().getValue() +
//                "  Ngong Chol Deng. All rights reserved.");
//        footer.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
//        footer.setAlignment(Pos.CENTER);
//        footer.setPadding(new Insets(15, 0, 0, 0));
//
//        VBox main = new VBox(30, scrollPane, slideshow, footer);
//        main.setAlignment(Pos.TOP_CENTER);
//        main.setPadding(new Insets(20));
//
//        AnimationUtils.applyFadeIn(this, 600);
//
//        this.setCenter(main);
//    }
//}


package com.ngong.librasoftware.view;

import com.ngong.librasoftware.utils.AnimationUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class HistoryView extends BorderPane {
    private int slideIndex = 0;

    public HistoryView() {
        this.setPadding(new Insets(20));

        // 🌟 Heading
        Label heading = new Label("📘 About Librasoftware");
        heading.setFont(Font.font("Segoe UI", 24));
        heading.setStyle("-fx-font-weight: bold;");

        // 👤 Developer Info
        VBox devInfo = new VBox(
                createSectionTitle("👨‍💻 Developer Info"),
                createWrappedLabel("• Developed by: Abraham N. Manyuon"),
                createWrappedLabel("• Location: Juba, South Sudan"),
                createWrappedLabel("• Project Started: 2025"),
                createWrappedLabel("• Academic Background:"),
                createWrappedLabel("   - In 2020, Abraham pursued a 9-month Advanced Certificate in Computer Studies at St. Vincent De Paul. Despite the disruptions caused by the COVID-19 pandemic, he persevered and successfully completed the program. The course covered foundational and practical skills in areas such as CCTV installation, computer networking, and basic design."),
                createWrappedLabel("   - In 2021, he enrolled in a Diploma in Information Technology at Don Bosco Vocational Training Centre. Over the course of two years, he developed strong technical and problem-solving skills, graduating in 2023 with distinction across all modules.")
        );
        devInfo.setSpacing(6);


        // 🎯 Purpose
        VBox purpose = new VBox(
                createSectionTitle("🎯 History View"),
                createWrappedLabel("Librasoftware simplifies and digitizes book borrowing for school librarians. It tracks checkouts, flags overdue returns, and allows for real-time student clearance status—cutting back paperwork and boosting accountability.")
        );
        purpose.setSpacing(4);

        // 🗂️ Categories


        // 📎 Footer
        Label footer = new Label("Librasoftware v1.0 – © " + java.time.Year.now().getValue() + " Ngong Manyuon Ngong. All rights reserved.");
        footer.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 0, 0));

        // 📜 Scrollable Content
        VBox content = new VBox(25, heading, devInfo, purpose, footer);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);
        content.setPrefWidth(800);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");

        AnimationUtils.applyFadeIn(this, 600);
        this.setCenter(scrollPane);
    }

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        return label;
    }

    private Label createWrappedLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(700);
        return label;
    }
}

