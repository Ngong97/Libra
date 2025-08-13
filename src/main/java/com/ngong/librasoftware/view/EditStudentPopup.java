package com.ngong.librasoftware.view;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.model.CheckInEntry;
import com.ngong.librasoftware.model.StudentRecord;
import com.ngong.librasoftware.utils.UIUtils;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.function.UnaryOperator;

public class EditStudentPopup extends Stage {
    private final Pane contentArea;



    public EditStudentPopup(StudentRecord student, Runnable onUpdate, Pane contentArea1) {
        this.contentArea = contentArea1;
        setTitle("Edit Student");
        UIUtils.applyAppIcon(this);
        this.initOwner(contentArea.getScene().getWindow());

        DatabaseService db = new DatabaseService();

        // Student Info
        TextField name = new TextField(student.nameProperty().get());
        name.setStyle("-fx-pref-width: 300;-fx-padding: 6 10;-fx-background-radius: 6;-fx-border-radius: 6;-fx-border-color: #ccc;-fx-background-color: #fff;-fx-font-size: 14;");
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }
            StringBuilder result = new StringBuilder();
            boolean capitalizeNext = true;
            for (char c : newText.toCharArray()) {
                if (Character.isWhitespace(c)) {
                    result.append(c);
                    capitalizeNext = true;
                } else if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
            change.setText(result.toString());
            change.setRange(0, change.getControlText().length());
            return change;
        };
        // Apply the formatter to the text field
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        name.setTextFormatter(textFormatter);




        TextField id = new TextField(student.getIdentity());
        id.setStyle("-fx-pref-width: 300;-fx-padding: 6 10;-fx-background-radius: 6;-fx-border-radius: 6;-fx-border-color: #ccc;-fx-background-color: #fff;-fx-font-size: 14;");
        id.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.equals(newText.toUpperCase())) {
                int caretPos = id.getCaretPosition();
                id.setText(newText.toUpperCase());
                id.positionCaret(caretPos); // preserve caret position
            }
        });

//        ComboBox<String> gender = new ComboBox<>(FXCollections.observableArrayList("Male", "Female"));
        ComboBox<String> gender = new ComboBox<>();
        gender.getItems().addAll(db.getAllStudentGenders());
        gender.setStyle("-fx-pref-width: 300;-fx-padding: 6 10;-fx-background-radius: 6;-fx-border-radius: 6;-fx-border-color: #ccc;-fx-background-color: #fff;-fx-font-size: 14;");
        gender.setValue(student.genderProperty().get());
        gender.setEditable(true);

// Create the TextFormatter
        UnaryOperator<TextFormatter.Change> genderfilter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;

            StringBuilder result = new StringBuilder();
            boolean capitalizeNext = true;
            for (char c : newText.toCharArray()) {
                if (Character.isWhitespace(c)) {
                    result.append(c);
                    capitalizeNext = true;
                } else if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }

            change.setText(result.toString());
            change.setRange(0, change.getControlText().length());
            return change;
        };

        TextFormatter<String> formatter = new TextFormatter<>(genderfilter);
        gender.getEditor().setTextFormatter(formatter);



        gender.addEventFilter(ScrollEvent.SCROLL, event -> {
            ObservableList<String> items = gender.getItems();
            int currentIndex = gender.getSelectionModel().getSelectedIndex();

            if (items.isEmpty()) return;

            if (event.getDeltaY() < 0) {
                // Scroll down → next gender
                int nextIndex = (currentIndex + 1) % items.size();
                gender.getSelectionModel().select(nextIndex);
            } else {
                // Scroll up → previous gender
                int prevIndex = (currentIndex - 1 + items.size()) % items.size();
                gender.getSelectionModel().select(prevIndex);
            }

            event.consume(); // prevent default scrolling behavior
        });

//        ComboBox<String> classBox = new ComboBox<>(FXCollections.observableArrayList("S1", "S2", "S3", "S4"));
        ComboBox<String> classBox = new ComboBox<>();

        classBox.getItems().addAll(db.getAllStudentClasses());

        classBox.setStyle("-fx-pref-width: 300;-fx-padding: 6 10;-fx-background-radius: 6;-fx-border-radius: 6;-fx-border-color: #ccc;-fx-background-color: #fff;-fx-font-size: 14;");
        classBox.setValue(student.studentClassProperty().get()); classBox.setEditable(true);

        classBox.addEventFilter(ScrollEvent.SCROLL, event -> {
            ObservableList<String> items = classBox.getItems();
            int currentIndex = classBox.getSelectionModel().getSelectedIndex();

            if (items.isEmpty()) return;

            if (event.getDeltaY() < 0) {
                // Scroll down → next gender
                int nextIndex = (currentIndex + 1) % items.size();
                classBox.getSelectionModel().select(nextIndex);
            } else {
                // Scroll up → previous gender
                int prevIndex = (currentIndex - 1 + items.size()) % items.size();
                classBox.getSelectionModel().select(prevIndex);
            }

            event.consume(); // prevent default scrolling behavior
        });



//        ComboBox<String> termBox = new ComboBox<>(FXCollections.observableArrayList("Term I", "Term II", "Term III"));
        ComboBox<String> termBox = new ComboBox<>();
        termBox.getItems().addAll(db.getAllStudentTerms());

        termBox.setStyle("-fx-pref-width: 300;-fx-padding: 6 10;-fx-background-radius: 6;-fx-border-radius: 6;-fx-border-color: #ccc;-fx-background-color: #fff;-fx-font-size: 14;");
        termBox.setValue(student.termProperty().get()); termBox.setEditable(true);

        termBox.addEventFilter(ScrollEvent.SCROLL, event -> {
            ObservableList<String> items = termBox.getItems();
            int currentIndex = termBox.getSelectionModel().getSelectedIndex();

            if (items.isEmpty()) return;

            if (event.getDeltaY() < 0) {
                // Scroll down → next gender
                int nextIndex = (currentIndex + 1) % items.size();
                termBox.getSelectionModel().select(nextIndex);
            } else {
                // Scroll up → previous gender
                int prevIndex = (currentIndex - 1 + items.size()) % items.size();
                termBox.getSelectionModel().select(prevIndex);
            }

            event.consume(); // prevent default scrolling behavior
        });


        GridPane studentForm = new GridPane();
//        studentForm.setCursor(Cursor.HAND);
        studentForm.setHgap(15);
        studentForm.setVgap(15);
        studentForm.setAlignment(Pos.CENTER);


        Label nameLabel = new Label("Name");
        nameLabel.setStyle("-fx-font-size: 14;-fx-font-weight: bold");
        Label idLabel = new Label("Student ID");
        idLabel.setStyle("-fx-font-size: 14;-fx-font-weight: bold");
        Label genderLabel = new Label("Gender");
        genderLabel.setStyle("-fx-font-size: 14;-fx-font-weight: bold");
        Label classLabel = new Label("Class");
        classLabel.setStyle("-fx-font-size: 14;-fx-font-weight: bold");
        Label termLabel = new Label("Term");
        termLabel.setStyle("-fx-font-size: 14;-fx-font-weight: bold");


        studentForm.addRow(0, nameLabel, name);
        studentForm.addRow(1, idLabel, id);
        studentForm.addRow(2, genderLabel, gender);
        studentForm.addRow(3, classLabel, classBox);
        studentForm.addRow(4, termLabel, termBox);

        // Borrowed Books
        ListView<CheckInEntry> bookList = new ListView<>();
        ObservableList<CheckInEntry> books =
                FXCollections.observableArrayList(db.getBorrowedBooksForCheckIn(Integer.parseInt(student.getStudentId())));
        bookList.setItems(books);
        bookList.setMaxHeight(140);

        TextField title = new TextField(),
                author = new TextField(),
                isbn = new TextField();
        bookList.setOnMouseClicked(e -> {
            var selected = bookList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                title.setText(selected.getBookTitle());
                author.setText(selected.getAuthor());
//                isbn.setText(""); // Optional if stored
                isbn.setText(selected.getIsbn()); // ✅ This was missing
            }
        });

        Button updateBook = new Button("✏️ Update");
        updateBook.setStyle("-fx-background-color: #c6cfdc;-fx-text-fill: #090909;-fx-font-weight: bold;-fx-font-size: 13px;-fx-background-radius: 6;-fx-border-radius: 6;-fx-border-color: transparent;");
        updateBook.setCursor(Cursor.HAND);
        updateBook.setOnAction(e -> {
            var selected = bookList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                db.updateBorrowedBookDetails(
                        selected.getBookId(),
                        title.getText().trim(),
                        author.getText().trim(),
                        isbn.getText().trim()
                );

                // Reload list after update
                bookList.setItems(FXCollections.observableArrayList(
                        db.getBorrowedBooksForCheckIn(Integer.parseInt(student.getStudentId()))
                ));
            }
        });

        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(javafx.scene.paint.Color.BLACK);

        DropShadow blueShadow = new DropShadow();
        blueShadow.setOffsetY(2.0);
        blueShadow.setColor(Color.BLUE); // Set shadow color and transparency


        updateBook.setEffect(blackShadow);
        updateBook.setOnMouseEntered(event -> {
            updateBook.setScaleX(1.1);
            updateBook.setEffect(blueShadow);
        });
        updateBook.setOnMouseExited(event -> {
            updateBook.setScaleX(1.0);
            updateBook.setEffect(blackShadow);
        });
        ScaleTransition pressUpdate = new ScaleTransition(Duration.millis(80), updateBook);
        pressUpdate.setToX(0.95);
        pressUpdate.setToY(0.95);

        ScaleTransition releaseUpdate = new ScaleTransition(Duration.millis(80), updateBook);
        releaseUpdate.setToX(1.0);
        releaseUpdate.setToY(1.0);

        updateBook.setOnMousePressed(e -> pressUpdate.play());
        updateBook.setOnMouseReleased(e -> releaseUpdate.play());


//        Button removeBook = new Button("🗑️ Remove");
//        removeBook.setCursor(Cursor.HAND);
//        removeBook.setStyle("-fx-background-color: #c6cfdc;-fx-text-fill: #090909;-fx-font-weight: bold;-fx-font-size: 13px;-fx-background-radius: 6;-fx-border-radius: 6;-fx-border-color: transparent;");
//        removeBook.setOnAction(e -> {
//            var sel = bookList.getSelectionModel().getSelectedItem();
//            if (sel != null) {
//                db.clearReturnedBooks(List.of(sel.getBorrowingId()));
//                books.remove(sel);
//            }
//        });
//
//        removeBook.setOnMouseEntered(event -> {
//            removeBook.setScaleX(1.1);
//            removeBook.setStyle("-fx-background-color: #4b4e53;-fx-text-fill: #fdfcfc;-fx-font-weight: bold;-fx-font-size: 13px;-fx-background-radius: 6;-fx-border-radius: 6;-fx-border-color: transparent;");
//        });
//
//        removeBook.setOnMouseExited(event -> {
//            removeBook.setScaleX(1.0);
//            removeBook.setStyle("-fx-background-color: #c6cfdc;-fx-text-fill: #090909;-fx-font-weight: bold;-fx-font-size: 13px;-fx-background-radius: 6;-fx-border-radius: 6;-fx-border-color: transparent;");
//        });
//


//        HBox bookButtons = new HBox(10, updateBook, removeBook);

        Label titleLabel= new Label("Title:");
        titleLabel.setStyle("-fx-font-weight: bold;");
        Label authorLabel= new Label("Author:");
        authorLabel.setStyle("-fx-font-weight: bold;");
        Label isbnLabel= new Label("ISBN:");
        isbnLabel.setStyle("-fx-font-weight: bold;");

        VBox bookEditor = new VBox(8,
                titleLabel, title,
                authorLabel, author,
                isbnLabel, isbn,
                updateBook
        );

        VBox bookLayout = new VBox(10, bookList, new TitledPane("Edit Selected Book", bookEditor));
        bookLayout.setStyle("-fx-font-size: 14");
        // Tabs
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                new Tab("👤 Student Info", studentForm),
                new Tab("📚 Borrowed Books", bookLayout)
        );

        // Save Button
        Button save = new Button("💾 Save");
        save.setCursor(Cursor.HAND);
        save.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(e -> {
            db.updateStudentDetails(
                    Integer.parseInt(student.getStudentId()),
                    name.getText().trim(),
                    id.getText().trim(),
                    gender.getValue(),
                    classBox.getValue()
            );
            db.updateBorrowingTerm(Integer.parseInt(student.getStudentId()), termBox.getValue()); // 👈 new call
            if (onUpdate != null) onUpdate.run();
            close();
        });


        VBox root = new VBox(15, tabs, save);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        setScene(new Scene(root, 500, 470));
        this.setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
    }


}
