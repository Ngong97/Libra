package com.ngong.librasoftware.view;


import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.model.BookInfo;
import com.ngong.librasoftware.model.SnackbarForRegistration;
import com.ngong.librasoftware.utils.AnimationUtils;
import com.ngong.librasoftware.utils.WarningMessage;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CheckOutView extends HBox {
    final String computer_username=System.getProperty("user.name");
    final String DB_URL = "jdbc:sqlite:C:\\Users\\"+computer_username+"\\libraDB\\libraDB.db";
    private int highlightedIndex = -1;


    private int highlightedIndex2 = -1;
    private int highlightedIndex3 = -1;


    Popup titleClickPopup = new Popup();
    VBox titleClickBox = new VBox();




    ComboBox<String> titleComboBox;
    private int selectedIndex = -1;
    private final Pane contentArea;
    StackPane root;
    SnackbarForRegistration snackbar;
    private final DatabaseService db = new DatabaseService();
    private final ObservableList<BookEntry2> selectedBooks = FXCollections.observableArrayList();
    private final ListView<String> bookList = new ListView<>(FXCollections.observableArrayList());



    private final Popup suggestionPopup = new Popup();
    private final VBox suggestionBox = new VBox(5);

    private final Popup bookPopup = new Popup();
    private final VBox bookBox = new VBox(5);



    public CheckOutView(Pane contentArea) {


        titleClickBox.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #ccc;
            -fx-border-radius: 8;
            -fx-font-size: 14;
            -fx-cursor: hand;
            -fx-background-radius: 8;
            -fx-padding: 8;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);
        """);
        titleClickBox.setPrefWidth(250);
        titleClickPopup.getContent().add(titleClickBox);
//        titleClickPopup.setAutoHide(true);
//        titleClickPopup.getContent().add(titleClickBox);

        suggestionBox.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #ccc;
            -fx-border-radius: 8;
            -fx-cursor: hand;
            -fx-font-size: 14;
            -fx-background-radius: 8;
            -fx-padding: 8;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);
        """);
        suggestionBox.setPrefWidth(250);
        suggestionPopup.getContent().add(suggestionBox);
        suggestionPopup.setAutoHide(true);
        bookBox.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #ccc;
            -fx-border-radius: 8;
            -fx-cursor: hand;
            -fx-font-size: 14;
            -fx-background-radius: 8;
            -fx-padding: 8;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);
        """);
        bookBox.setPrefWidth(250);
        bookPopup.getContent().add(bookBox);
        bookPopup.setAutoHide(true);




        DatabaseService db = new DatabaseService();
        this.contentArea = contentArea;
        this.setSpacing(40);
        this.setPadding(new Insets(24));


        CheckBox teacherCheckBox = new CheckBox("Is Teacher");
        teacherCheckBox.setStyle("-fx-font-weight: bold; -fx-padding: 4;");





        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency

        DropShadow blueShadow = new DropShadow();
        blueShadow.setOffsetY(2.0);
        blueShadow.setColor(Color.BLUE); // Set shadow color and transparency



        // --- Student Form ---
        VBox studentForm = new VBox(10);
        studentForm.setPrefWidth(400);


        ComboBox<String> studentGender = new ComboBox<>();



        TextField studentName = new TextField();
//        studentName.setMinWidth(300);
        studentName.setPromptText("Full Name");
        studentName.getStyleClass().add("settings-textfield");


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
        studentName.setTextFormatter(textFormatter);


        studentName.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isBlank()) {
                suggestionPopup.hide();
                return;
            }

            // 🔍 Extract the last word being typed
            String[] parts = newText.trim().split("\\s+");
            String lastWord = parts[parts.length - 1];

            if (lastWord.isBlank()) {
                suggestionPopup.hide();
                return;
            }

            List<String> matches = db.getStudentNamesStartingWith(lastWord);
            if (matches.isEmpty()) {
                suggestionPopup.hide();
                return;
            }
            highlightedIndex = -1;
            suggestionBox.getChildren().clear();
            for (String match : matches) {
                Label suggestion = new Label(match);
                suggestion.setStyle("""
                        -fx-padding: 6 10;
                        -fx-background-radius: 6;
                        -fx-cursor: hand;
                    """);

                suggestion.setOnMouseEntered(e -> suggestion.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 6 10; -fx-background-radius: 6;-fx-font-weight: bold"));
                suggestion.setOnMouseExited(e -> suggestion.setStyle("-fx-padding: 6 10; -fx-background-radius: 6;-fx-font-weight: normal"));

                suggestion.setOnMouseClicked(e -> {
                    // Replace only the last word with the selected suggestion
                    parts[parts.length - 1] = match;
                    String updated = String.join(" ", parts) + " ";
                    studentName.setText(updated);
                    studentName.positionCaret(updated.length());
                    suggestionPopup.hide();
                });

                suggestionBox.getChildren().add(suggestion);
            }

            if (!suggestionPopup.isShowing()) {
                Bounds bounds = studentName.localToScreen(studentName.getBoundsInLocal());
                suggestionPopup.show(studentName, bounds.getMinX(), bounds.getMaxY());
            }

            //tooltip if student exists
            Tooltip notClearedTip = new Tooltip("Student not cleared");
            notClearedTip.setStyle("-fx-font-size: 14; -fx-background-color: #f44336; -fx-text-fill: white;");

            if (newText.trim().isEmpty()) {
                studentName.setStyle(""); // Reset
                studentName.setTooltip(null);
                return;
            }

            boolean uncleared = db.hasUnreturnedBooks(newText.trim());

            if (uncleared) {
                studentName.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                studentName.setTooltip(notClearedTip);
                WarningMessage msg = new WarningMessage(
                        "Student won't be allowed due to unreturned books",
                        Duration.seconds(5),
                        contentArea // Your layout container
                );

                contentArea.getChildren().add(msg);
                StackPane.setAlignment(msg, Pos.CENTER);
            } else {
                studentName.setStyle(""); // Reset style
                studentName.setTooltip(null);
            }


            String name = studentName.getText().trim();
            String knownGender = db.getGenderFromMemory(name);
            if (knownGender != null) {
                studentGender.setValue(knownGender); // ✅ Autopopulate
            }


        });


        // Add this inside your initialization or setup method:
        studentName.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!suggestionPopup.isShowing()) return;

            int total = suggestionBox.getChildren().size();

            switch (event.getCode()) {
                case DOWN -> {
                    highlightedIndex = (highlightedIndex + 1) % total;
                    updateHighlight();
                    event.consume();
                }
                case UP -> {
                    highlightedIndex = (highlightedIndex - 1 + total) % total;
                    updateHighlight();
                    event.consume();
                }
                case TAB -> {
                    if (total == 1) {
                        Label selected = (Label) suggestionBox.getChildren().get(0);
                        applySuggestion(studentName, selected.getText());
                    } else {
                        highlightedIndex = (highlightedIndex + 1) % total;
                        updateHighlight();
                    }
                    event.consume(); // 💥 This now fully prevents focus jump
                }
                case ENTER -> {
                    if (highlightedIndex >= 0 && highlightedIndex < total) {
                        Label selected = (Label) suggestionBox.getChildren().get(highlightedIndex);
                        applySuggestion(studentName, selected.getText());
                        event.consume();
                    }
                }
            }
        });

        TextField studentID = new TextField();
        studentID.setPromptText("Student ID");
        studentID.getStyleClass().add("settings-textfield");

        studentID.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.equals(newText.toUpperCase())) {
                int caretPos = studentID.getCaretPosition();
                studentID.setText(newText.toUpperCase());
                studentID.positionCaret(caretPos); // preserve caret position
            }
        });

        String redBorder = "-fx-border-color: red; -fx-border-width: 2px;";
        String defaultBorderStyle = ""; // Or your base styling

        studentID.textProperty().addListener((obs, oldVal, newVal) -> {


            if (newVal.trim().isEmpty()) {
                studentID.setStyle(defaultBorderStyle);
                return;
            }

            boolean exists = db.doesStudentIdExist(newVal.trim());
            if (exists) {
                studentID.setStyle(redBorder);
                WarningMessage msg = new WarningMessage(
                        "A student with same Identity has not cleared",
                        Duration.seconds(5),
                        contentArea // Your layout container
                );

                contentArea.getChildren().add(msg);
                StackPane.setAlignment(msg, Pos.CENTER);

            } else {
                studentID.setStyle(defaultBorderStyle);
            }

        });



        studentGender.getItems().addAll(db.getAllStudentGenders());
        studentGender.setPromptText("Gender");
        studentGender.setEditable(true); // 👈 allows free typing
        studentGender.getStyleClass().add("settings-combo");
        studentGender.addEventFilter(ScrollEvent.SCROLL, event -> {
            ObservableList<String> items = studentGender.getItems();
            int currentIndex = studentGender.getSelectionModel().getSelectedIndex();

            if (items.isEmpty()) return;

            if (event.getDeltaY() < 0) {
                // Scroll down → next gender
                int nextIndex = (currentIndex + 1) % items.size();
                studentGender.getSelectionModel().select(nextIndex);
            } else {
                // Scroll up → previous gender
                int prevIndex = (currentIndex - 1 + items.size()) % items.size();
                studentGender.getSelectionModel().select(prevIndex);
            }

            event.consume(); // prevent default scrolling behavior
        });



        ComboBox<String> studentClass = new ComboBox<>();
        studentClass.getItems().addAll(db.getAllStudentClasses());
        studentClass.setPromptText("Class");
        studentClass.setEditable(true);
        studentClass.getStyleClass().add("settings-combo");

        studentClass.addEventFilter(ScrollEvent.SCROLL, event -> {
            ObservableList<String> items = studentClass.getItems();
            int currentIndex = studentClass.getSelectionModel().getSelectedIndex();

            if (items.isEmpty()) return;

            if (event.getDeltaY() < 0) {
                // Scroll down: next item
                int nextIndex = (currentIndex + 1) % items.size();
                studentClass.getSelectionModel().select(nextIndex);
            } else {
                // Scroll up: previous item
                int prevIndex = (currentIndex - 1 + items.size()) % items.size();
                studentClass.getSelectionModel().select(prevIndex);
            }

            event.consume(); // prevent default scroll behavior
        });



        ComboBox<String> termBox = new ComboBox<>();
        termBox.getItems().addAll(db.getAllStudentTerms());
        termBox.setPromptText("Term");
        termBox.getStyleClass().add("settings-combo");
        termBox.setEditable(true);

        String lastTerm = db.getLastUsedTerm(); // You create this method
        termBox.setValue(lastTerm != null ? lastTerm : "Term I");
//        studentForm.getChildren().add(termBox);

        termBox.addEventFilter(ScrollEvent.SCROLL, event -> {
            ObservableList<String> items = termBox.getItems();
            int currentIndex = termBox.getSelectionModel().getSelectedIndex();

            if (items.isEmpty()) return;

            if (event.getDeltaY() < 0) {
                // Scroll down → next term
                int nextIndex = (currentIndex + 1) % items.size();
                termBox.getSelectionModel().select(nextIndex);
            } else {
                // Scroll up → previous term
                int prevIndex = (currentIndex - 1 + items.size()) % items.size();
                termBox.getSelectionModel().select(prevIndex);
            }

            event.consume(); // prevent native scroll
        });



        Label studentDetails=new Label("🎓 Student Details");
        studentDetails.setStyle("-fx-font-weight: bold;-fx-font-size: 16px;");
        VBox topForm = new VBox(10);
        topForm.getChildren().addAll(
                studentDetails,
                studentName, studentID, studentGender, studentClass, termBox
        );


        // --- Book Entry Form ---
        VBox bookForm = new VBox(10);
        bookForm.setPrefWidth(400);
//        bookForm.setStyle("-fx-border-color: GREEN;-fx-border-width: 2px;-fx-border-radius: 15");

        ComboBox<String> quantityComboBox = new ComboBox<>();
        quantityComboBox.setPromptText("Copies");
        IntStream.rangeClosed(1, 200)
                .mapToObj(i -> i + " Copies")
                .forEach(quantityComboBox.getItems()::add);

        quantityComboBox.setValue("1 Copies");

        quantityComboBox.getStyleClass().add("settings-combo");
        quantityComboBox.setEditable(true);
        quantityComboBox.setVisible(false);
        quantityComboBox.setManaged(false); // Keeps layout clean when hidden


        quantityComboBox.addEventFilter(ScrollEvent.SCROLL, event -> {
            ObservableList<String> items = quantityComboBox.getItems();
            int currentIndex = quantityComboBox.getSelectionModel().getSelectedIndex();

            if (items.isEmpty() || currentIndex < 0) return;

            if (event.getDeltaY() > 0) {
                // Scroll up → increase quantity
                if (currentIndex < items.size() - 1) {
                    quantityComboBox.getSelectionModel().select(currentIndex + 1);
                }
            } else {
                // Scroll down → decrease quantity
                if (currentIndex > 0) {
                    quantityComboBox.getSelectionModel().select(currentIndex - 1);
                }
            }

            event.consume(); // suppress default scroll behavior
        });





        teacherCheckBox.setOnAction(e -> {
            boolean isTeacher = teacherCheckBox.isSelected();
            quantityComboBox.setVisible(isTeacher);
            quantityComboBox.setManaged(isTeacher); // keeps layout clean
            if (isTeacher) {
                studentID.setPromptText("Teacher ID");
            }else {
                quantityComboBox.setValue("1 Copies");
                studentID.setPromptText("Student ID");
            }
        });




        TextField bookTitleField = new TextField();
        bookTitleField.setPromptText("Book Title");
        bookTitleField.getStyleClass().add("settings-textfield");
//        HBox bookBox=new HBox();
//        bookBox.getChildren().addAll(bookTitleField,quantityComboBox);


        TextField author = new TextField(); author.setPromptText("Author");
        author.getStyleClass().add("settings-textfield");
        author.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F3 && event.isShiftDown()) {
                IndexRange selection = author.getSelection();

                if (selection.getLength() > 0) {
                    String selectedText = author.getSelectedText();
                    String capitalized = capitalizeEachWord(selectedText);

                    // Replace selection with capitalized version
                    StringBuilder newText = new StringBuilder(author.getText());
                    newText.replace(selection.getStart(), selection.getEnd(), capitalized);
                    author.setText(newText.toString());

                    // Reselect the updated portion
                    author.selectRange(selection.getStart(), selection.getStart() + capitalized.length());
                }
            }
        });


        bookTitleField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isBlank()) {
                bookPopup.hide();
                return;
            }

            String prefix = newText.trim();

            List<BookInfo> matches = db.getBookDetailsForTitleStartingWith(prefix);
            if (matches.isEmpty()) {
                bookPopup.hide();
                return;
            }

            highlightedIndex2 = -1;
            bookBox.getChildren().clear();

            for (BookInfo book : matches) {
                String book_title = book.getTitle();
                String book_author = book.getAuthor();

                Label suggestion = new Label(book_title + "  —  " + book_author);
                suggestion.setStyle("""
            -fx-padding: 6 10;
            -fx-background-radius: 6;
            -fx-cursor: hand;
        """);

                suggestion.setOnMouseEntered(e -> suggestion.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 6 10; -fx-background-radius: 6;-fx-font-weight: bold"));
                suggestion.setOnMouseExited(e -> suggestion.setStyle("-fx-padding: 6 10; -fx-background-radius: 6;-fx-font-weight: normal"));

                suggestion.setOnMouseClicked(e -> {
                    bookTitleField.setText(book_title);
                    bookTitleField.positionCaret(book_title.length());
                    author.setText(book_author);
                    bookPopup.hide();
                });

                bookBox.getChildren().add(suggestion);
            }

            if (!bookPopup.isShowing()) {
                Bounds bounds = bookTitleField.localToScreen(bookTitleField.getBoundsInLocal());
                bookPopup.show(bookTitleField, bounds.getMinX(), bounds.getMaxY());
            }
        });

        bookTitleField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!bookPopup.isShowing()) return;

            int total = bookBox.getChildren().size();

            switch (event.getCode()) {
                case DOWN -> {
                    highlightedIndex2 = (highlightedIndex2 + 1) % total;
                    updateHighlight2();
                    event.consume();
                }
                case UP -> {
                    highlightedIndex2 = (highlightedIndex2 - 1 + total) % total;
                    updateHighlight2();
                    event.consume();
                }
                case TAB -> {
                    if (total == 1) {
                        Label selected = (Label) bookBox.getChildren().get(0);
                        String[] split = selected.getText().split("  —  ");
                        String book_title = split[0];
                        String book_author = split.length > 1 ? split[1] : "";
                        bookTitleField.setText(book_title);
                        author.setText(book_author);
                        bookPopup.hide();
                    } else {
                        highlightedIndex2 = (highlightedIndex2 + 1) % total;
                        updateHighlight2();
                    }
                    event.consume();
                }
                case ENTER -> {
                    if (highlightedIndex2 >= 0 && highlightedIndex2 < total) {
                        Label selected = (Label) bookBox.getChildren().get(highlightedIndex2);
                        String[] split = selected.getText().split("  —  ");
                        String book_title = split[0];
                        String book_author = split.length > 1 ? split[1] : "";
                        bookTitleField.setText(book_title);
                        author.setText(book_author);
                        bookPopup.hide();
                        event.consume();
                    }
                    else if (total == 1) {
                        Label selected = (Label) bookBox.getChildren().get(0);
                        String[] split = selected.getText().split("  —  ");
                        String book_title = split[0];
                        String book_author = split.length > 1 ? split[1] : "";
                        bookTitleField.setText(book_title);
                        author.setText(book_author);
                        bookPopup.hide();
                    }

                }
            }
        });



        bookTitleField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!titleClickPopup.isShowing()) return;

            int total = titleClickBox.getChildren().size();

            switch (event.getCode()) {
                case DOWN -> {
                    highlightedIndex3 = (highlightedIndex3 + 1) % total;
                    updateHighlight3();
                    event.consume();
                }
                case UP -> {
                    highlightedIndex3 = (highlightedIndex3 - 1 + total) % total;
                    updateHighlight3();
                    event.consume();
                }
                case TAB -> {
                    if (total == 1) {
                        Label selected = (Label) titleClickBox.getChildren().get(0);
                        String[] split = selected.getText().split("  —  ");
                        String book_title = split[0];
                        String book_author = split.length > 1 ? split[1] : "";
                        bookTitleField.setText(book_title);
                        author.setText(book_author);
                        titleClickPopup.hide();
                    } else {
                        highlightedIndex3 = (highlightedIndex3 + 1) % total;
                        updateHighlight3();
                    }
                    event.consume();
                }
                case ENTER -> {
                    if (highlightedIndex3 >= 0 && highlightedIndex3 < total) {
                        Label selected = (Label) titleClickBox.getChildren().get(highlightedIndex3);
                        String[] split = selected.getText().split("  —  ");
                        String book_title = split[0];
                        String book_author = split.length > 1 ? split[1] : "";
                        bookTitleField.setText(book_title);
                        author.setText(book_author);
                        titleClickPopup.hide();
                        event.consume();
                    }
                }
            }
        });


        TextField isbn = new TextField();
        isbn.setPromptText("ISBN");
        isbn.getStyleClass().add("settings-textfield");

        String redBorderStyle = "-fx-border-color: red; -fx-border-width: 2px;";
        String defaultStyle = ""; // Or your app’s normal styling


        isbn.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.trim().isEmpty()) {
                isbn.setStyle(defaultStyle);
                return;
            }

            boolean exists = db.doesIsbnExist(newText.trim());
            if (exists) {
                isbn.setStyle(redBorderStyle);

                WarningMessage msg = new WarningMessage(
                        "Can not use ISBN, book with similar ISBN borrowed",
                        Duration.seconds(5),
                        contentArea // Your layout container
                );

                contentArea.getChildren().add(msg);
                StackPane.setAlignment(msg, Pos.CENTER);

            } else {
                isbn.setStyle(defaultStyle);
            }
        });


        TextField duration = new TextField();
        duration.setPromptText("0 days");
        duration.getStyleClass().add("settings-textfield");

        duration.setOnScroll(event -> {
            try {
                int currentValue = Integer.parseInt(duration.getText());

                if (event.getDeltaY() > 0) {
                    currentValue++; // Scroll up
                } else {
                    currentValue--; // Scroll down
                }

                if (currentValue < 0) {
                    currentValue = 0;
                }

                duration.setText(String.valueOf(currentValue));
                // ✅ Add red border if value > 14
                if (currentValue > 14) {
                    duration.setStyle("-fx-border-color: #e11111; -fx-border-width: 2px;");
                    WarningMessage msg = new WarningMessage(
                            "Isn't that too long to borrow a book?",
                            Duration.seconds(5),
                            contentArea
                    );
                    contentArea.getChildren().add(msg);
                    StackPane.setAlignment(msg, Pos.CENTER);
                } else {
                    duration.setStyle(""); // Reset style
                }

            } catch (NumberFormatException e) {
                // Optional: reset to 0 if input is invalid
                duration.setText("0");
            }
        });


        titleComboBox=new ComboBox<>();
        db.loadTitles(titleComboBox);
        titleComboBox.setPrefWidth(40);
        titleComboBox.setVisible(false);
        titleComboBox.setCenterShape(true);
        titleComboBox.setStyle("-fx-font-size: 16;-fx-font-family: 'Times New Roman'");


        bookTitleField.setOnMouseClicked(e -> {
            if (titleClickPopup.isShowing()) return;

            // 🔄 Always fetch latest on click
            List<BookInfo> books = db.getRecentlyBorrowedBooksWithAuthors();
            titleClickBox.getChildren().clear();

            for (BookInfo book : books) {
                String book_title = book.getTitle();
                String book_author = book.getAuthor();

                Label label = new Label(book_title + "  —  " + book_author);
                label.setStyle("""
            -fx-padding: 6 10;
            -fx-background-radius: 6;
            -fx-cursor: hand;
        """);

                label.setOnMouseEntered(ev -> label.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 6 10; -fx-background-radius: 6;-fx-font-weight: bold"));
                label.setOnMouseExited(ev -> label.setStyle("-fx-padding: 6 10; -fx-background-radius: 6;-fx-font-weight: normal"));

                label.setOnMouseClicked(ev -> {
                    bookTitleField.setText(book_title);
                    author.setText(book_author);
                    titleClickPopup.hide();
                });

                titleClickBox.getChildren().add(label);
            }

            if (!books.isEmpty()) {
                Bounds bounds = bookTitleField.localToScreen(bookTitleField.getBoundsInLocal());
                titleClickPopup.show(bookTitleField, bounds.getMinX(), bounds.getMaxY());
            }
        });

        bookTitleField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.equals(oldText)) {
                titleClickPopup.hide();
            }
        });


        bookTitleField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                titleClickPopup.hide();
            }
        });





        titleComboBox.setOnAction(event -> {
            String selectedTitle = titleComboBox.getSelectionModel().getSelectedItem();
            if (selectedTitle != null) {
                bookTitleField.setText(selectedTitle); // Populate title field authorField.setText(getAuthorByTitle(selectedTitle)); // Populate author field
                author.setText(db.getAuthorByTitleFromDictionary(selectedTitle));
            }
        });



        bookTitleField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F3 && event.isShiftDown()) {
                IndexRange selection = bookTitleField.getSelection();

                if (selection.getLength() > 0) {
                    String selectedText = bookTitleField.getSelectedText();
                    String capitalized = capitalizeEachWord(selectedText);

                    // Replace selection with capitalized version
                    StringBuilder newText = new StringBuilder(bookTitleField.getText());
                    newText.replace(selection.getStart(), selection.getEnd(), capitalized);
                    bookTitleField.setText(newText.toString());

                    // Reselect the updated portion
                    bookTitleField.selectRange(selection.getStart(), selection.getStart() + capitalized.length());
                }
            }
        });


        Button addBook = new Button("➕ Add Book");
        addBook.setCursor(Cursor.HAND);
        addBook.setOnAction(e -> {
            String newTitle = bookTitleField.getText();
            String newAuthor = author.getText();
//            String newIsbn = isbn.getText();

            String isbn_text = isbn.getText() == null || isbn.getText().trim().isEmpty()
                    ? db.generateFallbackIsbn(newTitle, newTitle)
                    : isbn.getText();

            String newDuration = duration.getText();
            int quantity = teacherCheckBox.isSelected() ? Integer.parseInt(quantityComboBox.getValue().replaceAll("[^0-9]", "")) : 1;
            System.out.println("⚠️ The quantity is: " + quantity);


                    if (!newTitle.isBlank() && !newAuthor.isBlank()) {
                        // Check for duplicates by title + author
                        boolean duplicate = selectedBooks.stream()
                                .anyMatch(book ->
                                        book.title.equalsIgnoreCase(newTitle.trim()) &&
                                                book.author.equalsIgnoreCase(newAuthor.trim())
                                );


                        boolean isbn_exists=db.doesIsbnExist(isbn_text);

                        if (duplicate) {

                            WarningMessage msg = new WarningMessage(
                                    "Same book already exists!",
                                    Duration.seconds(5),
                                    contentArea // Your layout container
                            );
                            contentArea.getChildren().add(msg);
                            StackPane.setAlignment(msg, Pos.CENTER);
                            return;
                        }else if (isbn_exists){
                            WarningMessage msg = new WarningMessage(
                                    "Cannot use this ISBN, that book was borrowed!",
                                    Duration.seconds(5),
                                    contentArea // Your layout container
                            );
                            contentArea.getChildren().add(msg);
                            StackPane.setAlignment(msg, Pos.CENTER);
                            return;
                        }

                        // Proceed to add book
                        String summary = newTitle + " — " + newAuthor+" ("+quantity+" Copies"+")";
                        bookList.getItems().add(summary);
//                        selectedBooks.add(new BookEntry2(newTitle, newAuthor, newIsbn, newDuration));
                        selectedBooks.add(new BookEntry2(newTitle, newAuthor, isbn_text, newDuration, quantity));
                        // Clear input fields


                        if (!db.recordExists(newTitle, newAuthor)) {
                            String description = null;

                            if (db.isConnectedToInternet()) {
                                description = db.fetchBookDescription(newTitle, newAuthor);
                            }

                            boolean descriptionFetched = description != null && !description.isBlank() && !description.equals("No description found");

                            try {
                                db.addToBookDictionary(newTitle, newAuthor, descriptionFetched ? description : null);
                            } catch (SQLException ex) {
                                if (ex.getMessage() != null && ex.getMessage().contains("UNIQUE constraint failed: bookDictionary.title")) {
                                    // Duplicate title — ignore and continue
                                } else {
                                    ex.printStackTrace(); // Log other errors
                                }
                            }


                            if (!descriptionFetched) {
                                db.addToPendingDescriptions(newTitle, newAuthor); // retryCount defaults to 0

                                WarningMessage popup = new WarningMessage(
                                        "📡 Connect to internet to load book descriptions!",
                                        Duration.seconds(4),
                                        contentArea
                                );
                                contentArea.getChildren().add(popup);
                                StackPane.setAlignment(popup, Pos.CENTER);
                            }
                        }

                        bookTitleField.clear(); author.clear(); isbn.clear(); duration.clear();
                        quantityComboBox.setValue("1 Copies");
                    }else {
                        WarningMessage msg = new WarningMessage(
                                "Cannot proceed, book details needed!",
                                Duration.seconds(5),
                                contentArea // Your layout container
                        );
                        contentArea.getChildren().add(msg);
                        StackPane.setAlignment(msg, Pos.CENTER);
                    }
        });




//        ImageView refreshIcon = new ImageView(new Image(getClass().getResource("/images/book-out.png").toExternalForm()));


        Button refresh = new Button();
        Image refreshIcon = new Image(getClass().getResource("/images/clear2.png").toString()); // Create a BackgroundImage with the loaded image
        ImageView refreshIconView = new ImageView(refreshIcon);
        refreshIconView.setFitWidth(40);
        refreshIconView.setFitHeight(40);
        refresh.setGraphic(refreshIconView);
        Tooltip refreshtooltip = new Tooltip("Clear Fields");
//        refreshtooltip.getStyleClass().add("nav-tooltip");
        refreshtooltip.setShowDelay(Duration.millis(100));
        refreshtooltip.setHideDelay(Duration.millis(100));
        Tooltip.install(refresh, refreshtooltip);
//        refresh.getStyleClass().add("Button-part");
        refresh.setCursor(Cursor.HAND);
        refresh.setEffect(blackShadow);


        ScaleTransition pressAnim = new ScaleTransition(Duration.millis(80), refresh);
        pressAnim.setToX(0.95);
        pressAnim.setToY(0.95);

        ScaleTransition releaseAnim = new ScaleTransition(Duration.millis(80), refresh);
        releaseAnim.setToX(1.0);
        releaseAnim.setToY(1.0);

        refresh.setOnMousePressed(e -> pressAnim.play());
        refresh.setOnMouseReleased(e -> releaseAnim.play());

        refresh.setOnAction(e -> {
            bookTitleField.clear();
            author.clear();
            isbn.clear();
            duration.clear();
            studentName.clear();
            studentID.clear();
            studentClass.setValue("");
            studentGender.setValue("");
        });



        //...........................................



        bookList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedIndex = bookList.getSelectionModel().getSelectedIndex();
            if (newVal != null) {
                for (BookEntry2 entry : selectedBooks) {
                    System.out.println("The quantity is: "+entry.quantity);

                    String summary = entry.title + " — " + entry.author + " ("+entry.quantity+" Copies"+")";
                    if (summary.equals(newVal)) {
                        bookTitleField.setText(entry.title);
                        author.setText(entry.author);
                        isbn.setText(entry.isbn);
                        duration.setText(entry.duration);
                        quantityComboBox.setValue(String.valueOf(entry.quantity));
                        break;
                    }
                }
            }
        });


        Button updateBook = new Button("✅ Update Book");
        updateBook.setCursor(Cursor.HAND);


        updateBook.setOnAction(e -> {
            if (selectedIndex >= 0 && selectedIndex < selectedBooks.size()) {
                BookEntry2 entry = selectedBooks.get(selectedIndex);
                entry.title = bookTitleField.getText();
                entry.author = author.getText();
                entry.isbn = isbn.getText();
                entry.duration = duration.getText();
                entry.quantity = Integer.parseInt(quantityComboBox.getValue().replaceAll("[^0-9]", ""));; // ✅ Now it's an int


                bookList.getItems().set(selectedIndex, entry.title + " — " + entry.author);

                bookTitleField.clear(); author.clear(); isbn.clear(); duration.clear();
                quantityComboBox.setValue(null);
                selectedIndex = -1;
                bookList.getSelectionModel().clearSelection();
            }
        });


        updateBook.setDisable(true);


//        HBox titleBox=new HBox();
//        titleBox.getChildren().addAll(title,titleComboBox);

        StackPane titleStack=new StackPane();
        titleStack.getChildren().add(bookTitleField);
        titleStack.getChildren().add(titleComboBox);
        titleStack.setAlignment(Pos.TOP_LEFT);



        Button deleteBook = new Button("🗑️ Delete Book");
        deleteBook.setCursor(Cursor.HAND);
//        deleteBook.setStyle("-fx-background-color: #E53935; -fx-text-fill: white;");
        deleteBook.setOnAction(e -> {
            String selected = bookList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedBooks.remove(selected);
                bookList.getItems().remove(selected);
            }
        });
        deleteBook.setDisable(true);

        HBox buttonBar = new HBox(10, addBook,updateBook,deleteBook);

        Label bookDetails=new Label("📚 Book(s) to Issue");
        bookDetails.setStyle("-fx-font-weight: bold;-fx-font-size: 16px;");
        bookForm.getChildren().addAll(
                bookDetails, titleStack, author, quantityComboBox,isbn, duration, buttonBar, bookList
        );

        // --- Submit button
        ImageView confirmIcon = new ImageView(new Image(getClass().getResource("/images/book-out.png").toExternalForm()));
        confirmIcon.setFitWidth(40);
        confirmIcon.setFitHeight(40);
        Button issueBooks = new Button();
        Tooltip tooltip = new Tooltip("✅ Confirm & Issue");
        tooltip.getStyleClass().add("nav-tooltip");
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setHideDelay(Duration.millis(100));
        Tooltip.install(issueBooks, tooltip);
        issueBooks.setCursor(Cursor.HAND);
        issueBooks.setGraphic(confirmIcon);
//        issueBooks.getStyleClass().add("Button-part");
        issueBooks.setEffect(blackShadow);

        ScaleTransition pressAn = new ScaleTransition(Duration.millis(100), issueBooks);
        pressAn.setToX(0.95);
        pressAn.setToY(0.95);

        ScaleTransition releaseAn = new ScaleTransition(Duration.millis(100), issueBooks);
        releaseAn.setToX(1.0);
        releaseAn.setToY(1.0);

        issueBooks.setOnMousePressed(e -> pressAn.play());
        issueBooks.setOnMouseReleased(e -> releaseAn.play());

        issueBooks.setOnAction(e -> {
            String student_name = studentName.getText();
            String student_Id =  studentID.getText();
            String student_gender = studentGender.getValue();
            String student_class = studentClass.getValue();
            String student_term = termBox.getValue();

            if (student_name.isEmpty() || student_Id.isEmpty() || student_gender.isEmpty()
                    || student_class.isEmpty() || student_term.isEmpty() || selectedBooks.isEmpty()) {
                WarningMessage msg = new WarningMessage(
                        "Please fill in all required fields!",
                        Duration.seconds(5),
                        contentArea // Your layout container
                );

                contentArea.getChildren().add(msg);
                StackPane.setAlignment(msg, Pos.CENTER);
                return;
            }else if (db.doesStudentIdExist(student_Id)){

                WarningMessage msg = new WarningMessage(
                        "Please, you can't reuse Identity without clearance!",
                        Duration.seconds(5),
                        contentArea // Your layout container
                );

                contentArea.getChildren().add(msg);
                StackPane.setAlignment(msg, Pos.CENTER);

                return;
            }

            // Check for blocked condition
            if (db.hasNameWithUnreturnedBooks(student_name)) {
                WarningMessage msg = new WarningMessage(
                         "Cannot proceed. "+student_name.toUpperCase() + " has uncleared books",
                        Duration.seconds(5),
                        contentArea // Your layout container
                );

                contentArea.getChildren().add(msg);
                StackPane.setAlignment(msg, Pos.CENTER);
                return;
            }

            // Proceed to save new borrowing
            db.storeUsedNameParts(student_name);

            for (BookEntry2 b : selectedBooks) {
                db.issueBookToStudent(student_Id, student_name, student_gender, student_class, student_term, b);
            }

            if (!db.nameGenderExists(student_name,student_gender)){
                db.saveGenderToMemory(student_name,student_gender);
            }



            bookList.getItems().clear();
            selectedBooks.clear();

            // Reset inputs
            studentName.clear();
            studentID.clear();
            studentGender.setValue("");
            studentClass.setValue("");

            WarningMessage msg = new WarningMessage(
                    "Details saved successfully!",
                    Duration.seconds(5),
                    contentArea // Your layout container
            );

            contentArea.getChildren().add(msg);
            StackPane.setAlignment(msg, Pos.CENTER);
        });



        VBox formColumn = new VBox(30);
        formColumn.setAlignment(Pos.TOP_CENTER); // align form fields up top


        Region topSpacer = new Region();
        Region bottomSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);



        HBox confirmBox = new HBox(40, issueBooks, refresh,teacherCheckBox);
        confirmBox.setAlignment(Pos.CENTER_LEFT);
        confirmBox.setPadding(new Insets(10, 0, 0, 0));

        VBox middleColumn = new VBox(20, topSpacer, confirmBox, bottomSpacer);
        middleColumn.setAlignment(Pos.CENTER);

        formColumn.getChildren().addAll(topForm, middleColumn);
        formColumn.setPrefWidth(400);



//        studentForm.getChildren().addAll( topForm,spacer,issueBooks);
        StackPane leftPane = new StackPane(formColumn);
        leftPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
//        studentForm.getChildren().addAll(issueBooks);




        Button editBook = new Button("✏️ Edit Book");
        editBook.setCursor(Cursor.HAND);
        editBook.setStyle("-fx-background-color: #FFDD57; -fx-text-fill: black;");
        editBook.setOnAction(e -> {
            String selected = bookList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedBooks.remove(selected);
                bookList.getItems().remove(selected);
            }
        });


        bookList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            updateBook.setDisable(!hasSelection);
            deleteBook.setDisable(!hasSelection);
        });

        AnimationUtils.applyFadeIn(this, 600);

        VBox rightPane = new VBox(20, bookForm);
        this.getChildren().addAll(leftPane, rightPane);

        this.setOnMouseClicked(e -> {
            bookPopup.hide();
            titleClickPopup.hide();
        });

        this.setAlignment(Pos.TOP_CENTER);


    }



    private String capitalizeEachWord(String input) {
        String[] words = input.trim().split("\\s+");
        return Arrays.stream(words)
                .map(word -> word.isEmpty() ? word :
                        Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }


    private void applySuggestion(TextField nameField,String match) {
        String[] parts = nameField.getText().trim().split("\\s+");
        parts[parts.length - 1] = match;
        String updated = String.join(" ", parts) + " ";
        nameField.setText(updated);
        nameField.positionCaret(updated.length());
        suggestionPopup.hide();
        highlightedIndex = -1;
    }

    private void updateHighlight() {
        for (int i = 0; i < suggestionBox.getChildren().size(); i++) {
            Label label = (Label) suggestionBox.getChildren().get(i);
            if (i == highlightedIndex) {
                label.setStyle("-fx-background-color: #407df4; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6;-fx-font-weight: bold");
            } else {
                label.setStyle("-fx-padding: 6 10; -fx-background-radius: 6;-fx-font-weight: normal");
            }
        }
    }


    private void updateHighlight2() {
        for (int i = 0; i < bookBox.getChildren().size(); i++) {
            Label label = (Label) bookBox.getChildren().get(i);
            if (i == highlightedIndex2) {
                label.setStyle("-fx-background-color: #407df4; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6;-fx-font-weight: bold");
            } else {
                label.setStyle("-fx-padding: 6 10; -fx-background-radius: 6;-fx-font-weight: normal");
            }
        }
    }

    private void updateHighlight3() {
        for (int i = 0; i < titleClickBox.getChildren().size(); i++) {
            Node node = titleClickBox.getChildren().get(i);
            if (node instanceof Label label) {
                if (i == highlightedIndex3) {
                    label.setStyle("-fx-background-color: #407df4; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6;-fx-font-weight: bold");
                } else {
                    label.setStyle("-fx-padding: 6 10; -fx-background-radius: 6;-fx-font-weight: normal");
                }
            }
        }
    }
    // A simple internal class to store each book’s entry
    public static class BookEntry2 {
        public String title, author, isbn, duration;
        public Integer quantity;
        BookEntry2(String t, String a, String i, String d, int quantity) {
            title = t; author = a; isbn = i; duration = d;
            this.quantity = quantity;
        }
    }
}
