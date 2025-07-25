package com.ngong.librasoftware.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;

public class UIUtils {

    public static Image loadExternalImage(String resourcePath) {
        URL imageUrl = UIUtils.class.getResource(resourcePath);
        if (imageUrl == null) {
            return null; // fallback or log missing resource
        }
        return new Image(imageUrl.toString());
    }

    public static ImageView loadExternalImageView(String resourcePath, double width, double height) {
        Image img = loadExternalImage(resourcePath);
        if (img == null) return new ImageView(); // placeholder
        ImageView view = new ImageView(img);
        view.setFitWidth(width);
        view.setFitHeight(height);
        return view;
    }

    public static void applyAppIcon(Stage stage) {
        Image icon = new Image(UIUtils.class.getResource("/images/books.png").toString());
        stage.getIcons().add(icon);
    }

}


