package com.ngong.librasoftware.view;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.model.ReportData;
import com.ngong.librasoftware.service.ReportService;
import javafx.animation.ScaleTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ReportView extends VBox {
    DatabaseService db=new DatabaseService();


    String defaultSchool = db.getCurrentUserSchool();
    String defaultLibrarian = db.getCurrentUserFullName();

    Map<String, String> defaults = db.getDefaultLibrarianInfo();

//schoolField.setPromptText("Enter School Name");
//librarianField.setPromptText("Enter Librarian Name");

    WebView pdfPreview = new WebView();

      // Hidden until preview is generated

    public ReportView() {
        ImageView pdfPreviewImage = new ImageView();
        pdfPreviewImage.setFitWidth(600);
        pdfPreviewImage.setPreserveRatio(true);
        pdfPreviewImage.setVisible(false);



        TextField schoolField = new TextField();
        TextField librarianField = new TextField();

        schoolField.setPromptText("School Name (optional)");
        librarianField.setPromptText("Librarian Name (optional)");

        VBox optionalDetailsBox = new VBox(10, schoolField, librarianField);
        optionalDetailsBox.setPadding(new Insets(10));

        TitledPane optionalDetailsPane = new TitledPane("Optional Details", optionalDetailsBox);
        optionalDetailsPane.setExpanded(false); // collapsed by default
        optionalDetailsPane.setMaxWidth(400);
        pdfPreview.setPrefHeight(500); // Adjust as needed
        pdfPreview.setVisible(false);

        this.setPadding(new Insets(20));
        this.setSpacing(15);

        Label heading = new Label("📄 Generate Library Report");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");




        Button previewBtn = new Button("Preview");
        previewBtn.setCursor(Cursor.HAND);
        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(javafx.scene.paint.Color.BLACK);

        DropShadow blueShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLUE); // Set shadow color and transparency

        previewBtn.setEffect(blackShadow);
        previewBtn.setOnMouseEntered(event -> {
            previewBtn.setScaleX(1.1);
            previewBtn.setEffect(blueShadow);
        });
        previewBtn.setOnMouseExited(event -> {
            previewBtn.setScaleX(1.0);
            previewBtn.setEffect(blackShadow);
        });
        ScaleTransition pressUpdate = new ScaleTransition(Duration.millis(80), previewBtn);
        pressUpdate.setToX(0.95);
        pressUpdate.setToY(0.95);

        ScaleTransition releaseUpdate = new ScaleTransition(Duration.millis(80), previewBtn);
        releaseUpdate.setToX(1.0);
        releaseUpdate.setToY(1.0);

        previewBtn.setOnMousePressed(e -> pressUpdate.play());
        previewBtn.setOnMouseReleased(e -> releaseUpdate.play());


        Button saveBtn = new Button("Save Report");

        saveBtn.setCursor(Cursor.HAND);


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

        ReportData data = db.fetchReportData();





        previewBtn.setOnAction(e -> {
            // Use typed value if present, otherwise fallback to default
            String schoolInput = schoolField.getText().trim();
            String librarianInput = librarianField.getText().trim();

            String school = schoolInput.isEmpty() ? defaultSchool : schoolInput;
            String librarian = librarianInput.isEmpty() ? defaultLibrarian : librarianInput;

            try {
                File tempFile = File.createTempFile("Library_Report_Preview", ".pdf");
                boolean success = ReportService.generateReport(tempFile, school, librarian, data);

                if (success) {
                    renderPdfPreview(tempFile, pdfPreviewImage);
                } else {
                    showAlert(false, "Failed to generate preview.");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                showAlert(false, "Error creating preview file.");
            }
        });




        saveBtn.setOnAction(e -> {
            String schoolInput = schoolField.getText().trim();
            String librarianInput = librarianField.getText().trim();

            String school = schoolInput.isEmpty() ? defaultSchool : schoolInput;
            String librarian = librarianInput.isEmpty() ? defaultLibrarian : librarianInput;
            // Format timestamped filename
            String timestamp = java.time.LocalDateTime.now()
                    .toString()
                    .replace("T", "_")
                    .replace(":", "-")
                    .substring(0, 16);
            String defaultFileName = "Library_Report_" + timestamp;

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Report");

            // Add both PDF and DOCX options
            FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF (*.pdf)", "*.pdf");
            FileChooser.ExtensionFilter docxFilter = new FileChooser.ExtensionFilter("Word Document (*.docx)", "*.docx");
            fileChooser.getExtensionFilters().addAll(pdfFilter, docxFilter);
            fileChooser.setSelectedExtensionFilter(pdfFilter); // default to PDF

            // Set default directory and filename
            File defaultDir = new File(System.getProperty("user.home"), "Documents");
            fileChooser.setInitialDirectory(defaultDir.exists() ? defaultDir : new File(System.getProperty("user.home")));
            fileChooser.setInitialFileName(defaultFileName + ".pdf");

            File destination = fileChooser.showSaveDialog(this.getScene().getWindow());

            if (destination != null) {
                try {
                    boolean success = ReportService.generateReport(destination, school, librarian, data);
                    if (success) {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(destination);
                        }
                    } else {
                        showAlert(false, "Failed to generate report.");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showAlert(false, "Failed to save report.");
                }
            }
        });


//        HBox buttonBox = new HBox(10, previewBtn, saveBtn, emailBtn);
//        this.setAlignment(Pos.TOP_LEFT);
        this.setAlignment(Pos.TOP_CENTER);



//        HBox buttonBox = new HBox(10, previewBtn, saveBtn, emailBtn);
//        buttonBox.setAlignment(Pos.CENTER_LEFT);

        this.getChildren().addAll(
                heading,
                new HBox(10, previewBtn, saveBtn,optionalDetailsPane),
                new Separator(),
                new Label("📄 PDF Preview:"),
                pdfPreviewImage
        );



    }
    private void renderPdfPreview(File pdfFile, ImageView imageView) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage bufferedImage = renderer.renderImageWithDPI(0, 100); // First page, 150 DPI

            WritableImage fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setImage(fxImage);
            imageView.setFitHeight(600);
            imageView.setFitWidth(700);
            imageView.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(false, "Failed to render PDF preview.");
        }
    }

    private void showAlert(boolean success, String message) {
        Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR, message);
        alert.show();
    }
}
