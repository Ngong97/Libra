package com.ngong.librasoftware.view;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
//import nl.siegmann.epublib.domain.Book;
//import nl.siegmann.epublib.domain.Resource;
//import nl.siegmann.epublib.epub.EpubReader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ReaderView extends Application {
    private WebView webView;
    private Stage stage;
    private boolean darkMode = false;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        webView = new WebView();
        webView.setZoom(1.2);

        // 🔍 Search
        TextField searchField = new TextField();
        Button searchBtn = new Button("Find");
        searchBtn.setOnAction(e -> {
            String script = "window.find('" + searchField.getText() + "')";
            webView.getEngine().executeScript(script);
        });

        // 🔍 Zoom
        Button zoomIn = new Button("Zoom +");
        zoomIn.setOnAction(e -> webView.setZoom(webView.getZoom() + 0.1));

        Button zoomOut = new Button("Zoom -");
        zoomOut.setOnAction(e -> webView.setZoom(webView.getZoom() - 0.1));

        // 🌓 Dark Mode
        Button toggleDark = new Button("Dark Mode");
        toggleDark.setOnAction(e -> {
            darkMode = !darkMode;
            String css = darkMode
                    ? "document.body.style.backgroundColor='black'; document.body.style.color='white';"
                    : "document.body.style.backgroundColor='white'; document.body.style.color='black';";
            webView.getEngine().executeScript(css);
        });

        // 🖥️ Fullscreen
        Button fullscreen = new Button("Fullscreen");
        fullscreen.setOnAction(e -> stage.setFullScreen(!stage.isFullScreen()));

        // 📂 Open Local File
        Button openLocal = new Button("Open Local Book");
        openLocal.setOnAction(e -> openLocalFile());

        // 💾 Download
        Button downloadBtn = new Button("Download Book");
        downloadBtn.setOnAction(e -> downloadCurrentBook());

        HBox controls = new HBox(10, searchField, searchBtn, zoomIn, zoomOut, toggleDark, fullscreen, openLocal, downloadBtn);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER);

        VBox layout = new VBox(controls, webView);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, #2196f3, 20, 0.5, 0, 4);");

        Scene scene = new Scene(layout, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("📖 ReaderView");
        primaryStage.show();
    }

    public void loadBookByTitle(String title) {
        String url = fetchOnlineResource(title);
        if (url != null) {
            webView.getEngine().load(url);
        } else {
            webView.getEngine().loadContent("<h2>No online resource found for: " + title + "</h2>");
        }
    }

    private String fetchOnlineResource(String title) {
        try {
            String query = URLEncoder.encode(title, StandardCharsets.UTF_8);
            URL url = new URL("https://openlibrary.org/search.json?title=" + query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String json = reader.lines().collect(Collectors.joining());
            reader.close();

            JSONObject obj = new JSONObject(json);
            JSONArray docs = obj.getJSONArray("docs");
            if (!docs.isEmpty()) {
                String key = docs.getJSONObject(0).getString("key");
                return "https://openlibrary.org" + key;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void openLocalFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Local Book");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("HTML/Text Files", "*.html", "*.htm", "*.txt"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("EPUB Files", "*.epub")
        );

        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            String path = file.toURI().toString();
            String name = file.getName().toLowerCase();

            if (name.endsWith(".html") || name.endsWith(".htm") || name.endsWith(".txt")) {
                webView.getEngine().load(path);
            } else if (file.getName().toLowerCase().endsWith(".pdf")) {
                openPDFFile(file); // Use PDFBox
            }  else {
                webView.getEngine().load(file.toURI().toString()); // For .html or .txt
            }

        }
    }




    private void openPDFFile(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 150); // First page, 150 DPI
            WritableImage fxImage = SwingFXUtils.toFXImage(image, null);

            ImageView imageView = new ImageView(fxImage);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(800);

            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setFitToWidth(true);

            Scene scene = new Scene(scrollPane, 900, 700);
            Stage pdfStage = new Stage();
            pdfStage.setScene(scene);
            pdfStage.setTitle("📘 PDF Viewer");
            pdfStage.initModality(Modality.APPLICATION_MODAL);
            pdfStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open PDF file.");
        }
    }

    private void showAlert(String error, String s) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(error);
        alert.setContentText(s);
        alert.showAndWait();
    }


    private void downloadCurrentBook() {
        String url = webView.getEngine().getLocation();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Book");
        chooser.setInitialFileName("book.html");
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try (InputStream in = new URL(url).openStream();
                 FileOutputStream out = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
