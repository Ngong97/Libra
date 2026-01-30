module com.ngong.librasoftware {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires javafx.media;
    requires org.apache.poi.ooxml;
    requires java.desktop;
    requires org.xerial.sqlitejdbc;
    requires java.prefs;
//    requires mysql.connector.j;
    requires javafx.swing;
    requires itextpdf;
    requires jakarta.mail;
    requires org.apache.pdfbox;
    requires org.slf4j;
    requires org.json;
    requires java.net.http;
    requires java.compiler;
    requires webcam.capture;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires com.google.gson;
//    requires epublib.core;
//    requires eu.hansolo.toolboxfx;
//    requires eu.hansolo.tilesfx;

    opens com.ngong.librasoftware to javafx.fxml;
    exports com.ngong.librasoftware;
    exports com.ngong.librasoftware.DAO;
    opens com.ngong.librasoftware.DAO to javafx.fxml;
    exports com.ngong.librasoftware.Controller;
    opens com.ngong.librasoftware.Controller to javafx.fxml;
    exports com.ngong.librasoftware.model;
    opens com.ngong.librasoftware.model to javafx.fxml;
    exports com.ngong.librasoftware.view;
}