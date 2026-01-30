package com.ngong.librasoftware.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URI;

public class DocumentationViewer {
    public static void launchDocumentationWindow() {
        Stage stage = new Stage();
        stage.setTitle("Libra Inventory Documentation");





        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setPrefWidth(800);
        content.getStyleClass().add("documentation-box");

        // Section generator
        content.getChildren().addAll(
                createSection("🧭 Overview", "Libra is a desktop library management tool built to empower librarians, educators, and administrators with intuitive workflows and powerful data handling."),
                createSection("⚙️ System Requirements", """
                • Windows 10 or later
                • Java JDK 11+
                • At least 2GB RAM
                • Minimum resolution: 1280×720
                """),
                createSection("🚀 Getting Started", """
                • Run LibraSetup.exe or launch Libra.jar
                • First-time setup prompts for school/librarian info
                • Working directory: C:/Users/<your_name>/libraDB
                """),
                createSection("📚 Core Features", """
                • Student registration with smart gender/class input
                • Real-time borrowing and safe returns
                • ISBN validation and fuzzy author matching
                • Quantity-aware book handling and undo logic
                """),
                createSection("📄 Reports", """
                • Generate PDF/DOCX reports with optional school/librarian info
                • Preview reports live inside Libra
                • Reports auto-name using timestamp
                """),
                createSection("🔐 Backup & Restore", """
                • Automated timestamped backups
                • Safe .bat restore tool with pre/post backup handling
                • Manual recovery options via restore folder
                """),
                createSection("💡 Tips & Tricks", """
                • Hover over book titles for cover + description
                • Use Dashboard to monitor active borrowing and trends
                """),
                createSection("🛠️ Troubleshooting", """
                • App won't launch → check Java installation
                • Book not found → use Add Book manually
                • Restore failed → verify newDB.db location
                • PDF preview blank → check JavaFX WebView support
                """)
        );

        VBox contactSection = new VBox(10);

        Label title = new Label("📩 Contact & License");
        title.getStyleClass().add("doc-heading");

        Label dev = new Label("Developer: Abraham N. Manyuon");
        Hyperlink email = createEmailLink("joncopy7@gmail.com");

        Label license = new Label("""
    Libra is proprietary software. Redistribution/modification without written consent is prohibited.
    For educational use, contact the author for permission.
""");
        license.getStyleClass().add("doc-body");
        license.setWrapText(true);

        contactSection.getChildren().addAll(title, dev, email, license);
        contactSection.getStyleClass().add("doc-section");

        content.getChildren().add(contactSection);



        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(scroll);
        scene.getStylesheets().add(DocumentationViewer.class.getResource("/documentation.css").toExternalForm());
        stage.setScene(scene);
        stage.setWidth(850);
        stage.setHeight(700);
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.show();
    }

    private static Hyperlink createEmailLink(String email) {
        Hyperlink link = new Hyperlink("Email: " + email);
        link.setStyle("-fx-text-fill: #1976D2; -fx-underline: true;");

        link.setOnAction(e -> {
            try {
                URI uri = new URI("https://mail.google.com/mail/?view=cm&fs=1&to=joncopy7@gmail.com");
                java.awt.Desktop.getDesktop().browse(uri);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return link;
    }


    private static VBox createSection(String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("doc-heading");

        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("doc-body");
        bodyLabel.setWrapText(true);

        VBox section = new VBox(10, titleLabel, bodyLabel);
        section.getStyleClass().add("doc-section");
        return section;
    }
}
