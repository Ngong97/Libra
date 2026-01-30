package com.ngong.librasoftware.view;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.model.Book;
import com.ngong.librasoftware.model.BookEntry;
import com.ngong.librasoftware.utils.AnimationUtils;
import com.ngong.librasoftware.utils.UIUtils;
import com.ngong.librasoftware.utils.WarningMessage;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooksView extends VBox {
    private boolean hasAnimatedRows = false;

    private final Popup bookHoverPopup = new Popup();
    private final VBox bookHoverContent = new VBox();
    private Book currentBookInPopup = null;  // 🚩 Tracks the book currently shown
    private final PauseTransition popupHideDelay = new PauseTransition(Duration.millis(250));

    private final DatabaseService db = new DatabaseService();
    private final ObservableList<Book> books = FXCollections.observableArrayList();
    private final Label registeredLabel = new Label();
    private final Label borrowedLabel = new Label();
    private final Label availableLabel = new Label();

    private final Pane contentArea;

    private List<String> originalLines = new ArrayList<>();


    @SuppressWarnings("unchecked")
    public BooksView(Pane contentArea) {
        setupBookHoverPopup();
        this.setPadding(new Insets(20));
        this.setSpacing(10);
        this.getStyleClass().add("books");
        this.contentArea = contentArea;


        DropShadow blueShadow = new DropShadow();
        blueShadow.setOffsetY(2.0);
        blueShadow.setColor(Color.BLUE); // Set shadow color and transparency


        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency


        Label title = new Label("📚 Library Inventory");
        title.setFont(new Font("Matura MT Script Capitals", 20));

        TextField search = new TextField();
        search.setPromptText("Search by title or author...");
        search.getStyleClass().add("search-bar");
        search.setMinWidth(300);

        CheckBox selectFilteredCheckbox = new CheckBox("Select All");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));


        TableView<Book> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No books found"));

        TableColumn<Book, String> serialNumCol = new TableColumn<>("S/N");
        serialNumCol.setCellValueFactory(new PropertyValueFactory<>("serialNum"));
        serialNumCol.setMaxWidth(40);
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setMaxWidth(400);
        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setMaxWidth(400);

        TableColumn<Book, Integer> totalCol = new TableColumn<>("Quantity");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalQuantity"));
        totalCol.setMaxWidth(120);

        TableColumn<Book, Integer> borrowedCol = new TableColumn<>("Borrowed");
        borrowedCol.setCellValueFactory(new PropertyValueFactory<>("borrowed"));
        borrowedCol.setMaxWidth(100);
        TableColumn<Book, Integer> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        availableCol.setMaxWidth(100);
        table.getColumns().addAll(serialNumCol,titleCol, authorCol, totalCol, borrowedCol, availableCol);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setCursor(Cursor.HAND);
        table.setPrefHeight(600);

        books.setAll(db.getAllLibraryBooksWithAvailability());
        updateInventorySummary();

        FilteredList<Book> filtered = new FilteredList<>(books, b -> true);
        search.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal.toLowerCase();
            filtered.setPredicate(book ->
                    book.getTitle().toLowerCase().contains(filter) ||
                            (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(filter))
            );
            selectFilteredCheckbox.setSelected(false);
        });
        table.setItems(filtered);


        // Checkbox logic
        selectFilteredCheckbox.setOnAction(e -> {
            if (selectFilteredCheckbox.isSelected()) {
                table.getSelectionModel().clearSelection();
                table.getItems().forEach(table.getSelectionModel()::select);
            } else {
                table.getSelectionModel().clearSelection();
            }
        });


        table.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Book clicked = row.getItem();
                    table.getSelectionModel().clearSelection(); // Optional
                    table.getSelectionModel().select(clicked);  // Makes it the selected item
                    editSelectedBooks(table);
                }
            });

            row.setOnMouseEntered(event -> {
                Book book = row.getItem();
                showBookHoverPopup(event, book); // 🖱️ pass the mouse event
            });

            row.setOnMouseExited(event -> {
                bookHoverPopup.hide();
            });


            return row;
        });



//        ImageView addIcon = new ImageView(new Image(getClass().getResource("/images/book-out.png").toExternalForm()));
        ImageView addIcon = UIUtils.loadExternalImageView("/images/book-out.png",30,30);


        Button addBooksBtn = new Button();
        addBooksBtn.getStyleClass().add("unique-button");
        addBooksBtn.setGraphic(addIcon);
        addBooksBtn.setEffect(blackShadow);
        addBooksBtn.setCursor(Cursor.HAND);

        Tooltip addBtnTooltip=new Tooltip("Add Book(s)");
        addBtnTooltip.getStyleClass().add("tool-tip");
        addBooksBtn.setTooltip(addBtnTooltip);
        addBtnTooltip.setShowDelay(Duration.millis(100));
        addBtnTooltip.setHideDelay(Duration.millis(100));
        addBtnTooltip.getStyleClass().add("tool-tip");


        addBooksBtn.setOnMouseEntered(event -> {
            addBooksBtn.setScaleX(1.1);
        });

        addBooksBtn.setOnMouseExited(event -> {
            addBooksBtn.setScaleX(1.0);
        });

        addBooksBtn.setOnAction(e -> showBulkAddDialog());




//        ImageView delIcon = new ImageView(new Image(getClass().getResource("/images/deleteIcon.png").toExternalForm()));
        ImageView delIcon = UIUtils.loadExternalImageView("/images/deleteIcon.png",30,30);



        Hyperlink syncWithBorrowed = new Hyperlink("Sync With Borrowed");
        syncWithBorrowed.setOnAction(e -> showBulkAddDialogFromBorrowings());


        Button deleteBtn = new Button();
        deleteBtn.setOnAction(e -> deleteSelectedBooks(table));
        deleteBtn.setGraphic(delIcon);
        deleteBtn.setCursor(Cursor.HAND);
        Tooltip deleteBtnTooltip=new Tooltip();
        deleteBtnTooltip.setText("Delete Selected");
        deleteBtn.setTooltip(deleteBtnTooltip);
        deleteBtnTooltip.setShowDelay(Duration.millis(100));
        deleteBtnTooltip.setHideDelay(Duration.millis(100));
        deleteBtnTooltip.getStyleClass().add("tool-tip");
        deleteBtn.setEffect(blackShadow);

        deleteBtn.setOnMouseEntered(event -> {
            deleteBtn.setScaleX(1.1);
        });

        deleteBtn.setOnMouseExited(event -> {
            deleteBtn.setScaleX(1.0);
        });


//        ImageView editIcon = new ImageView(new Image(getClass().getResource("/images/editIcon.png").toExternalForm()));
        ImageView editIcon = UIUtils.loadExternalImageView("/images/editIcon.png",30,30);




        Button editBtn = new Button();
        editBtn.setGraphic(editIcon);
        editBtn.setCursor(Cursor.HAND);
        editBtn.setOnAction(e -> {
            if (!table.getSelectionModel().getSelectedItems().isEmpty()){
                editSelectedBooks(table);
            }else {
                WarningMessage msg = new WarningMessage(
                        "Please select book(s) to edit!",
                        Duration.seconds(5),
                        contentArea
                );
                contentArea.getChildren().add(msg);
                StackPane.setAlignment(msg, Pos.CENTER);
            }
        });

        Tooltip editBtnTooltip=new Tooltip();
        editBtnTooltip.setText("Edit Selected");
        editBtnTooltip.setShowDelay(Duration.millis(100));
        editBtnTooltip.setHideDelay(Duration.millis(100));
        editBtnTooltip.getStyleClass().add("tool-tip");
        Tooltip.install(editBtn, editBtnTooltip);


//        editBtn.setTooltip(editBtnTooltip);

        editBtn.setEffect(blackShadow);

        editBtn.setOnMouseEntered(event -> {
            editBtn.setScaleX(1.1);
//            copyTable.setCursor(Cursor.HAND);
            editBtn.setEffect(blueShadow);
        });

        editBtn.setOnMouseExited(event -> {
            editBtn.setScaleX(1.0);
            editBtn.setEffect(blackShadow);
        });




        Button copyTable = new Button();
//        ImageView copy = new ImageView(new Image(getClass().getResource("/images/copyIcon.png").toExternalForm()));
        ImageView copy = UIUtils.loadExternalImageView("/images/copyIcon.png",30,30);

        copyTable.setGraphic(copy);
        copyTable.setCursor(Cursor.HAND);

        Tooltip copyTableTooltip=new Tooltip("Copy the table");
        Tooltip.install(copyTable,copyTableTooltip);
        copyTableTooltip.setShowDelay(Duration.millis(100));
        copyTableTooltip.setHideDelay(Duration.millis(100));
        copyTableTooltip.getStyleClass().add("tool-tip");
        copyTable.setEffect(blackShadow);

        copyTable.setOnMouseEntered(event -> {
            copyTable.setScaleX(1.1);
//            copyTable.setCursor(Cursor.HAND);
            copyTable.setEffect(blueShadow);
        });

        copyTable.setOnMouseExited(event -> {
            copyTable.setScaleX(1.0);
            copyTable.setEffect(blackShadow);
        });

        copyTable.setOnAction(e -> {
            copyTableToClipboard(table);
        });

        HBox buttonBar = new HBox(20,syncWithBorrowed, addBooksBtn,deleteBtn,editBtn,copyTable);
//        buttonBar.setSpacing(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);



        HBox summaryBox = createInventorySummaryBox();

//        ImageView searchIcon = new ImageView(new Image(getClass().getResource("/images/search.png").toExternalForm()));
        ImageView searchIcon = UIUtils.loadExternalImageView("/images/search.png",16,16);


        // Wrap them in a styled container
        StackPane searchContainer = new StackPane(search);
        StackPane.setAlignment(searchIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(searchIcon, new Insets(0, 12, 0, 0)); // right padding
        searchContainer.getChildren().add(searchIcon);
        searchContainer.getStyleClass().add("search-container");


//        HBox rightSearch = new HBox(8, nameSearch,searchIcon);
        HBox rightSearch = new HBox(searchContainer);

        rightSearch.setAlignment(Pos.CENTER_RIGHT);
        rightSearch.setPadding(new Insets(10));

        // Left side: action buttons
        HBox leftActions = new HBox(summaryBox,new Label());
        leftActions.setAlignment(Pos.CENTER_LEFT);


// Combined header bar
        BorderPane headerBar = new BorderPane();
        headerBar.setLeft(leftActions);
        headerBar.setCenter(selectFilteredCheckbox);
        headerBar.setRight(rightSearch);
        headerBar.setPadding(new Insets(10, 10, 0, 10));

        this.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        this.getChildren().addAll(title,headerBar, table, buttonBar);
        AnimationUtils.applyFadeIn(this, 600);
    }

    private void setupBookHoverPopup() {
        bookHoverContent.setPadding(new Insets(10));
        bookHoverContent.setStyle("""
        -fx-background-color: white;
        -fx-border-color: #ccc;
        -fx-border-radius: 10;
        -fx-background-radius: 10;
        -fx-padding: 12;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);
        -fx-font-size: 12px;
    """);
        bookHoverContent.setPrefWidth(300);
        bookHoverPopup.getContent().add(bookHoverContent);
        bookHoverPopup.setAutoHide(true);
    }


    private void showBookHoverPopup(MouseEvent event, Book book) {
        if (book == null) return;

        bookHoverContent.getChildren().clear();

        String bookTitle = book.getTitle();
        String author = db.getAuthorForBookFromBookDictionary(bookTitle);
        String description = db.getShortDescription(bookTitle);
        String imagePath = db.getCoverImagePath(bookTitle);
        Image coverImage;
        try {
            coverImage = UIUtils.loadExternalImage(imagePath);
        } catch (Exception e) {
            coverImage = UIUtils.loadExternalImage("/images/default_cover.png");
        }

        ImageView cover = new ImageView(coverImage);
        cover.setFitWidth(60);
        cover.setFitHeight(90);
        cover.setPreserveRatio(true);

        Label titleLabel = new Label(bookTitle);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label authorLabel = new Label("By " + author);
        authorLabel.setStyle("-fx-text-fill: #555;-fx-font-weight: bold;");

        Label descLabel = new Label(description != null ? description : "No description available.");
        descLabel.setWrapText(true);
//        descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12");
        descLabel.setMaxWidth(200);

        VBox left = new VBox(cover);
        VBox right = new VBox(4, titleLabel, authorLabel, descLabel);
        HBox header = new HBox(10, left, right);

        bookHoverContent.getChildren().add(header);

        // ✨ Position: bottom-left of popup at top-right of pointer, with some offset

        double mouseX = event.getScreenX();
        double mouseY = event.getScreenY();
        double screenWidth = Screen.getPrimary().getBounds().getWidth();

// Estimate popup dimensions
        bookHoverPopup.getScene().getRoot().applyCss(); // ensure layout pass
        double popupWidth = bookHoverContent.prefWidth(-1);
        double popupHeight = bookHoverContent.prefHeight(-1);

// If cursor is close to the right edge → flip left
        boolean overflowRight = mouseX + popupWidth + 30 > screenWidth;
        double offsetX = overflowRight ? -popupWidth - 15 : 15;
        double offsetY = 10;

        double popupX = mouseX + offsetX;
        double popupY = mouseY - popupHeight - offsetY;

        bookHoverPopup.show(((Node) event.getSource()), popupX, popupY);



//        bookHoverPopup.show(((Node) event.getSource()), popupX, popupY);
    }


    private void copyTableToClipboard(TableView<Book> table) {
        StringBuilder clipboardString = new StringBuilder();

        // Start the HTML table structure
        clipboardString.append("<html><body><table border='1'>");

        // Add the table headers
        clipboardString.append("<tr><th>S/N</th><th>Book Title</th><th>Author</th><th>Registered Quantity</th><th>Borrowed</th><th>Available</th></tr>");

        // Add each row of data
        for (Book result : table.getItems()) {
            clipboardString.append("<tr>")
                    .append("<td>").append(result.getSerialNum()).append("</td>")
                    .append("<td>").append(result.getTitle()).append("</td>")
                    .append("<td>").append(result.getAuthor()).append("</td>")
                    .append("<td>").append(result.getTotalQuantity()).append("</td>")
                    .append("<td>").append(result.getBorrowed()).append("</td>")
                    .append("<td>").append(result.getAvailable()).append("</td>")
                    .append("</tr>");
        }

        // Close the table and HTML tags
        clipboardString.append("</table></body></html>");

        // Create ClipboardContent with the HTML string
        ClipboardContent content = new ClipboardContent();
        content.putHtml(clipboardString.toString());  // Use the putHtml method to set HTML content
        Clipboard.getSystemClipboard().setContent(content);



        WarningMessage msg = new WarningMessage(
                "Table copied to Clipboard!",
                Duration.seconds(5),
                contentArea
        );
        contentArea.getChildren().add(msg);
        StackPane.setAlignment(msg, Pos.CENTER);
    }


    private void editSelectedBooks(TableView<Book> booksView) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Selected Books to Inventory");
        dialog.getDialogPane().setPrefWidth(700);
        dialog.initOwner(this.contentArea.getScene().getWindow());



        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK);
        // TextArea for entries
        TextArea input = new TextArea();
        input.setStyle("-fx-font-size: 16; -fx-line-spacing: 8;-fx-prompt-text-fill: #888");
        input.setPromptText("e.g.\nThings Fall Apart - Chinua Achebe = 5");
        
// Per-textarea counter (cycles through 0 → 1 → 2)
        IntegerProperty shiftF3Cycle = new SimpleIntegerProperty(0);

        input.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F3 && event.isShiftDown()) {
                IndexRange selection = input.getSelection();
                if (selection.getLength() > 0) {
                    String selectedText = input.getSelectedText();
                    String transformed;

                    int cycle = shiftF3Cycle.get() % 3;
                    switch (cycle) {
                        case 0 -> transformed = capitalizeLinesPreservingFormat(selectedText);
                        case 1 -> transformed = selectedText.toUpperCase();
                        case 2 -> transformed = selectedText.toLowerCase();
                        default -> transformed = selectedText;
                    }

                    // Replace selection
                    input.replaceText(selection.getStart(), selection.getEnd(), transformed);

                    // Reselect updated text
                    input.selectRange(selection.getStart(), selection.getStart() + transformed.length());

                    shiftF3Cycle.set(cycle + 1); // Move to next cycle
                }

                event.consume(); // prevent further propagation
            }
        });


        // Autofill selected rows and save original lines for comparison
        ObservableList<Book> selectedBooks = booksView.getSelectionModel().getSelectedItems();
        StringBuilder sb = new StringBuilder();
        originalLines.clear();
        for (Book book : selectedBooks) {
            String line = book.getTitle() + " - " + book.getAuthor() + " = " + book.getTotalQuantity();
            sb.append(line).append("\n");
            originalLines.add(line); // save original line
        }
        input.setText(sb.toString().trim());

        // Create MenuItems for dropdown (Transform tools)
        MenuItem toUpperItem = new MenuItem("UPPER CASE");
        MenuItem toLowerItem = new MenuItem("lower case");
        MenuItem capitalizeItem = new MenuItem("Capitalize Each");

        toUpperItem.setOnAction(e -> transformSelection(input, String::toUpperCase));
        toLowerItem.setOnAction(e -> transformSelection(input, String::toLowerCase));
        capitalizeItem.setOnAction(e -> transformSelection(input, NgongUtils::capitalizeEachWord));

        MenuButton toolsDropdown = new MenuButton("Aa", null,
                toUpperItem, toLowerItem, capitalizeItem);
        toolsDropdown.setStyle("-fx-font-size: 14;-fx-background-color: #96b2e1;-fx-font-weight: bold");
        Tooltip.install(toolsDropdown, new Tooltip("Change case"));


        Button pasteClipboard = new Button();
//        Image pasteIcon = new Image(getClass().getResource("/images/pasteClipboard.png").toString()); // Create a BackgroundImage with the loaded image
        ImageView pasteIcon = UIUtils.loadExternalImageView("/images/pasteClipboard.png",20,20);

        pasteClipboard.setGraphic(pasteIcon);
        Tooltip pastetooltip = new Tooltip("Paste (Ctrl+V)");
//        pastetooltip.getStyleClass().add("nav-tooltip");
        pastetooltip.setShowDelay(Duration.millis(100));
        pastetooltip.setHideDelay(Duration.millis(100));
        Tooltip.install(pasteClipboard, pastetooltip);
//        pasteClipboard.getStyleClass().add("Button-part");
        pasteClipboard.setCursor(Cursor.HAND);
        pasteClipboard.setEffect(blackShadow);

        Button clearAll = new Button();
//        Image refreshIcon = new Image(getClass().getResource("/images/clear.png").toString()); // Create a BackgroundImage with the loaded image
        ImageView refreshIcon = UIUtils.loadExternalImageView("/images/clear.png",20,20);

        clearAll.setGraphic(refreshIcon);
        Tooltip refreshtooltip = new Tooltip("Clear Text Editor");
//        refreshtooltip.getStyleClass().add("nav-tooltip");
        refreshtooltip.setShowDelay(Duration.millis(100));
        refreshtooltip.setHideDelay(Duration.millis(100));
        Tooltip.install(clearAll, refreshtooltip);
        clearAll.getStyleClass().add("Button-part");
        clearAll.setCursor(Cursor.HAND);
        clearAll.setEffect(blackShadow);




        pasteClipboard.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasString()) {
                input.insertText(input.getCaretPosition(), clipboard.getString());
            }
        });

        clearAll.setOnAction(e -> input.clear());

        HBox toolsBar = new HBox(10, toolsDropdown, pasteClipboard, clearAll);
        toolsBar.setAlignment(Pos.CENTER_LEFT);

        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.setStyle("-fx-text-fill: red;");

        ButtonType okButtonType = ButtonType.OK;
        ButtonType cancelButtonType = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setCursor(Cursor.HAND);
        Node cancelButton = dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setCursor(Cursor.HAND);
        HBox buttonRow = new HBox(10, okButton, cancelButton);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(12,
                new Label("📝 Review and edit selected books before adding:"),
                toolsBar,
                input,
                feedback,
                buttonRow
        );
        content.setPadding(new Insets(12));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == okButtonType) {
                List<BookEntry> editedEntries = parseBookEntries(input.getText(), feedback);
                if (!editedEntries.isEmpty() && originalLines.size() == editedEntries.size()) {
                    for (int i = 0; i < originalLines.size(); i++) {
                        // parse original line to get original title & author
                        BookEntry origEntry = parseSingleLineToBookEntry(originalLines.get(i));
                        BookEntry editedEntry = editedEntries.get(i);

                        db.updateLibraryBooks(
                                origEntry.title(),
                                origEntry.author(),
                                editedEntry.title(),
                                editedEntry.author(),
                                editedEntry.quantity()
                        );
                    }
                    books.setAll(db.getAllLibraryBooksWithAvailability());
                    updateInventorySummary();

                    WarningMessage msg = new WarningMessage(
                            "Edited successfully!",
                            Duration.seconds(5),
                            contentArea
                    );
                    contentArea.getChildren().add(msg);
                    StackPane.setAlignment(msg, Pos.CENTER);
                } else {
                    feedback.setText("⚠️ Number of edited entries does not match original selection.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private String capitalizeLinesPreservingFormat(String input) {
        return Arrays.stream(input.split("\n"))
                .map(this::capitalizeWords)
                .collect(Collectors.joining("\n"));
    }

    private String capitalizeWords(String input) {
        return Arrays.stream(input.trim().split("\\s+"))
                .map(word -> word.isEmpty() ? word :
                        Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }



    private BookEntry parseSingleLineToBookEntry(String line) {
        String[] eqParts = line.trim().split("=", 2);
        if (eqParts.length != 2 || !eqParts[1].trim().matches("\\d+")) {
            return null;
        }
        int quantity = Integer.parseInt(eqParts[1].trim());

        String[] dashParts = eqParts[0].trim().split("\\s*-\\s*", 2);
        if (dashParts.length != 2) {
            return null;
        }
        String title = dashParts[0].trim();
        String author = dashParts[1].trim();

        if (title.isEmpty() || author.isEmpty() || quantity <= 0) {
            return null;
        }
        return new BookEntry(title, author, quantity);
    }




    private void transformSelection(TextArea area, UnaryOperator<String> transformer) {
        IndexRange range = area.getSelection();
        String selected = area.getSelectedText();
        if (!selected.isEmpty()) {
            area.replaceText(range, transformer.apply(selected));
        }
    }

    public class NgongUtils {
        public static String capitalizeEachWord(String input) {
            return Arrays.stream(input.split("\n"))
                    .map(line -> Arrays.stream(line.trim().split("\\s+"))
                            .map(word -> word.isEmpty() ? word
                                    : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                            .collect(Collectors.joining(" "))
                    )
                    .collect(Collectors.joining("\n"));
        }

    }


    private void deleteSelectedBooks(TableView<Book> table) {
        List<Book> selectedBooks = table.getSelectionModel().getSelectedItems();

        if (selectedBooks == null || selectedBooks.isEmpty()) {
            WarningMessage msg = new WarningMessage(
                    "Please select something to delete!",
                    Duration.seconds(5),
                    contentArea
            );
            contentArea.getChildren().add(msg);
            StackPane.setAlignment(msg, Pos.CENTER);
            return;
        }

        String bookList = selectedBooks.stream()
                .map(b -> "• " + b.getTitle() + " by " + b.getAuthor())
                .collect(Collectors.joining("\n"));

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Are you sure you want to delete the selected books?");
        confirm.setContentText(bookList);
        confirm.initOwner(contentArea.getScene().getWindow());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (Book book : selectedBooks) {
                db.deleteLibraryBookById(book.getId());
            }
            books.setAll(db.getAllLibraryBooksWithAvailability());
            updateInventorySummary();

            WarningMessage msg = new WarningMessage(
                    "Deleted successfully!",
                    Duration.seconds(5),
                    contentArea
            );
            contentArea.getChildren().add(msg);
            StackPane.setAlignment(msg, Pos.CENTER);
        }
    }




    private void showBulkAddDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Register New Books");
        dialog.getDialogPane().setPrefWidth(500);
        dialog.initOwner(this.contentArea.getScene().getWindow());



        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency


        // Text input area
        TextArea input = new TextArea();
        input.setStyle("-fx-font-size: 16;-fx-line-spacing: 8;-fx-prompt-text-fill: #888");
        input.setPromptText("e.g.\nThings Fall Apart - Chinua Achebe = 5\nAtomic Habits - James Clear = 3");

        IntegerProperty shiftF3Cycle = new SimpleIntegerProperty(0);


        input.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F3 && event.isShiftDown()) {
                IndexRange selection = input.getSelection();
                if (selection.getLength() > 0) {
                    String selectedText = input.getSelectedText();
                    String transformed;

                    int cycle = shiftF3Cycle.get() % 3;
                    switch (cycle) {
                        case 0 -> transformed = capitalizeLinesPreservingFormat(selectedText);
                        case 1 -> transformed = selectedText.toUpperCase();
                        case 2 -> transformed = selectedText.toLowerCase();
                        default -> transformed = selectedText;
                    }

                    // Replace selection
                    input.replaceText(selection.getStart(), selection.getEnd(), transformed);

                    // Reselect updated text
                    input.selectRange(selection.getStart(), selection.getStart() + transformed.length());

                    shiftF3Cycle.set(cycle + 1); // Move to next cycle
                }

                event.consume(); // prevent further propagation
            }
        });



        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.setStyle("-fx-text-fill: red;");

        // Manual category input
        TextField categoryInput = new TextField();
        categoryInput.setPromptText("Type category or select below...");
        categoryInput.setPrefWidth(240);
        categoryInput.getStyleClass().add("settings-textfield");

        // Create MenuItems for dropdown (Transform tools)
        MenuItem fiction = new MenuItem("Fiction");
        MenuItem science = new MenuItem("Science");
        MenuItem history = new MenuItem("History");
        MenuItem biography = new MenuItem("Biography");
        MenuItem technology = new MenuItem("Technology");
        MenuItem philosophy = new MenuItem("Philosophy");
        MenuItem education = new MenuItem("Education");

        fiction.setOnAction(e -> categoryInput.setText(fiction.getText()));
        science.setOnAction(e ->categoryInput.setText(science.getText()));
        history.setOnAction(e -> categoryInput.setText(history.getText()));
        biography.setOnAction(e -> categoryInput.setText(biography.getText()));
        technology.setOnAction(e ->categoryInput.setText(technology.getText()));
        philosophy.setOnAction(e -> categoryInput.setText(philosophy.getText()));
        education.setOnAction(e -> categoryInput.setText(education.getText()));

        MenuButton categoryDropdown = new MenuButton("Category", null,
                fiction, science, history,biography,technology,philosophy,education);
        categoryDropdown.setStyle("-fx-font-size: 14;-fx-background-color: #96b2e1;-fx-font-weight: bold");
        Tooltip.install(categoryDropdown, new Tooltip("Choose category"));


        // Keep both in sync


        // Load Samples Online button
//        ImageView netIcon = new ImageView(new Image(getClass().getResource("/images/network.png").toExternalForm()));
        ImageView netIcon = UIUtils.loadExternalImageView("/images/network.png",30,30);


        Button loadSamplesBtn = new Button();
        loadSamplesBtn.setGraphic(netIcon);
        Tooltip deleteBtnTooltip=new Tooltip();
        deleteBtnTooltip.setText("Load Samples Online");
        loadSamplesBtn.setTooltip(deleteBtnTooltip);
        deleteBtnTooltip.setShowDelay(Duration.millis(100));
        deleteBtnTooltip.setHideDelay(Duration.millis(100));
        deleteBtnTooltip.getStyleClass().add("tool-tip");
        loadSamplesBtn.setEffect(blackShadow);
        loadSamplesBtn.setCursor(Cursor.HAND);

        loadSamplesBtn.setOnAction(e -> {
            input.setText("⏳ Loading...");
            String category = categoryInput.getText().isBlank() ? "fiction" : categoryInput.getText();

            new Thread(() -> {
                List<String> samples = generateSampleBookEntriesFromAPI(category);
                Platform.runLater(() -> input.setText(String.join("\n", samples)));
            }).start();
        });


//        ImageView fileIcon = new ImageView(new Image(getClass().getResource("/images/fileIcon.png").toExternalForm()));
        ImageView fileIcon = UIUtils.loadExternalImageView("/images/fileIcon.png",30,30);



        Button importBtn = new Button();
        importBtn.setGraphic(fileIcon);
        Tooltip importBtnTooltip=new Tooltip();
        importBtnTooltip.setText("Load From File");
        importBtn.setTooltip(importBtnTooltip);
        importBtnTooltip.setShowDelay(Duration.millis(100));
        importBtnTooltip.setHideDelay(Duration.millis(100));
        importBtnTooltip.getStyleClass().add("tool-tip");
        importBtn.setEffect(blackShadow);
        importBtn.setCursor(Cursor.HAND);

        importBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Import Book List");

            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.csv"),
                    new FileChooser.ExtensionFilter("Word Documents", "*.docx")
            );

            File file = chooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                try {
                    String content;

                    if (file.getName().endsWith(".docx")) {
                        content = readDocxContent(file);
                    } else {
                        content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                    }

                    input.setText(content);
                } catch (Exception ex) {
                    feedback.setText("❌ Failed to read file: " + ex.getMessage());
                }
            }
        });




        // OK and Cancel buttons
        ButtonType okButtonType = ButtonType.OK;
        ButtonType cancelButtonType = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setCursor(Cursor.HAND);
        Node cancelButton = dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setCursor(Cursor.HAND);

        // Buttons row
        HBox buttonRow = new HBox(10, loadSamplesBtn,importBtn, okButton, cancelButton);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);


        // Create MenuItems for dropdown (Transform tools)
        MenuItem toUpperItem = new MenuItem("UPPER CASE");
        MenuItem toLowerItem = new MenuItem("lower case");
        MenuItem capitalizeItem = new MenuItem("Capitalize Each");

        toUpperItem.setOnAction(e -> transformSelection(input, String::toUpperCase));
        toLowerItem.setOnAction(e -> transformSelection(input, String::toLowerCase));
        capitalizeItem.setOnAction(e -> transformSelection(input, NgongUtils::capitalizeEachWord));

        MenuButton toolsDropdown = new MenuButton("Aa", null,
                toUpperItem, toLowerItem, capitalizeItem);
        toolsDropdown.setStyle("-fx-font-size: 14;-fx-background-color: #96b2e1;-fx-font-weight: bold");
        Tooltip.install(toolsDropdown, new Tooltip("Change case"));




        HBox categoryBox=new HBox(10,toolsDropdown,categoryInput,categoryDropdown);
        categoryBox.setAlignment(Pos.CENTER_RIGHT);
        // Assemble the layout
        VBox content = new VBox(10,
                new Label("📝 Format: Title - Author = Quantity"),
                categoryBox,
                input,
                feedback,
                buttonRow
        );
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == okButtonType) {
                List<BookEntry> entries = parseBookEntries(input.getText(), feedback);
                if (!entries.isEmpty()) {
                    entries.forEach(entry ->{
                        db.addLibraryBookIfMissing(entry.title(), entry.author(), entry.quantity());

                        //...............
                        if (!db.recordExists(entry.title(), entry.author())) {
                            String description = null;

                            if (db.isConnectedToInternet()) {
                                description = db.fetchBookDescription(entry.title(), entry.author());


                                boolean descriptionFetched = description != null && !description.isBlank() && !description.equals("No description found");

                                try {
                                    db.addToBookDictionary(entry.title(), entry.author(),descriptionFetched ? description : null);
                                } catch (SQLException ex) {
                                    if (ex.getMessage() != null && ex.getMessage().contains("UNIQUE constraint failed: bookDictionary.title")) {
                                        // Duplicate title — ignore and continue
                                    } else {
                                        ex.printStackTrace(); // Log other errors
                                    }
                                }
                                if (!descriptionFetched) {
                                    db.addToPendingDescriptions(entry.title(), entry.author()); // retryCount defaults to 0
                                }


                            }else {
                                db.addToPendingDescriptions(entry.title(), entry.author()); // retryCount defaults to 0
                            }
                        }

                        //...............
                    });
                    books.setAll(db.getAllLibraryBooksWithAvailability());
                    updateInventorySummary();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private String readDocxContent(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {

            StringBuilder builder = new StringBuilder();
            for (XWPFParagraph para : doc.getParagraphs()) {
                builder.append(para.getText()).append("\n");
            }
            return builder.toString().trim();
        }
    }


    private void showBulkAddDialogFromBorrowings() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Sync with Borrowed Books");
        dialog.initOwner(this.contentArea.getScene().getWindow());

//        Image icon = new Image(UIUtils.class.getResource("/images/books.png").toString());
//        dialog.setGraphic(new ImageView(icon));

        dialog.getDialogPane().setPrefWidth(500);

        TextArea input = new TextArea();
        input.setStyle("-fx-font-size: 16;-fx-line-spacing: 8;-fx-prompt-text-fill: #888");
        input.setPromptText("e.g.\nThings Fall Apart - Chinua Achebe = 5");

        IntegerProperty shiftF3Cycle = new SimpleIntegerProperty(0);


        input.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F3 && event.isShiftDown()) {
                IndexRange selection = input.getSelection();
                if (selection.getLength() > 0) {
                    String selectedText = input.getSelectedText();
                    String transformed;

                    int cycle = shiftF3Cycle.get() % 3;
                    switch (cycle) {
                        case 0 -> transformed = capitalizeLinesPreservingFormat(selectedText);
                        case 1 -> transformed = selectedText.toUpperCase();
                        case 2 -> transformed = selectedText.toLowerCase();
                        default -> transformed = selectedText;
                    }

                    // Replace selection
                    input.replaceText(selection.getStart(), selection.getEnd(), transformed);

                    // Reselect updated text
                    input.selectRange(selection.getStart(), selection.getStart() + transformed.length());

                    shiftF3Cycle.set(cycle + 1); // Move to next cycle
                }

                event.consume(); // prevent further propagation
            }
        });

        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.setStyle("-fx-text-fill: red;");


        // Autofill borrowed books
        List<BookEntry> borrowedEntries = db.getUniqueBorrowedBooks(); // See step 3 below
        StringBuilder sb = new StringBuilder();
        borrowedEntries.forEach(entry -> sb.append(entry.title()).append(" - ")
                .append(entry.author()).append(" = ").append(entry.quantity()).append("\n"));
        input.setText(sb.toString().trim());

        ButtonType okButtonType = ButtonType.OK;
        ButtonType cancelButtonType = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setCursor(Cursor.HAND);
        Node cancelButton = dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setCursor(Cursor.HAND);
        HBox buttonRow = new HBox(10, okButton, cancelButton);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);


        // Create MenuItems for dropdown (Transform tools)
        MenuItem toUpperItem = new MenuItem("UPPER CASE");
        MenuItem toLowerItem = new MenuItem("lower case");
        MenuItem capitalizeItem = new MenuItem("Capitalize Each");

        toUpperItem.setOnAction(e -> transformSelection(input, String::toUpperCase));
        toLowerItem.setOnAction(e -> transformSelection(input, String::toLowerCase));
        capitalizeItem.setOnAction(e -> transformSelection(input, NgongUtils::capitalizeEachWord));

        MenuButton toolsDropdown = new MenuButton("Aa", null,
                toUpperItem, toLowerItem, capitalizeItem);
        toolsDropdown.setStyle("-fx-font-size: 14;-fx-background-color: #96b2e1;-fx-font-weight: bold");
        Tooltip.install(toolsDropdown, new Tooltip("Change case"));



        VBox content = new VBox(10,
                new HBox(10,toolsDropdown,
                        new Label("📝 Review and edit before adding to inventory:")),
                input,
                feedback,
                buttonRow
        );
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == okButtonType) {
                List<BookEntry> entries = parseBookEntries(input.getText(), feedback);
                if (!entries.isEmpty()) {
                    entries.forEach(entry -> db.addLibraryBookIfMissing(entry.title(), entry.author(), entry.quantity()));
                    books.setAll(db.getAllLibraryBooksWithAvailability());
                    updateInventorySummary();


                    WarningMessage msg = new WarningMessage(
                            "Sync successful!",
                            Duration.seconds(5),
                            contentArea
                    );
                    contentArea.getChildren().add(msg);
                    StackPane.setAlignment(msg, Pos.CENTER);


                }
            }
            return null;
        });

        dialog.showAndWait();
    }


    private List<String> generateSampleBookEntriesFromAPI(String category) {
        List<String> entries = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            String encoded = URLEncoder.encode(category, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://openlibrary.org/search.json?q=" + encoded))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());
            JSONArray docs = json.getJSONArray("docs");

            int added = 0;
            Random rand = new Random();
            for (int i = 0; i < docs.length() && added < 15; i++) {
                JSONObject book = docs.getJSONObject(i);
                String title = book.optString("title");
                JSONArray authors = book.optJSONArray("author_name");
                if (title != null && authors != null && authors.length() > 0) {
                    String author = authors.getString(0);
                    int quantity = rand.nextInt(10) + 1;
                    entries.add(title + " - " + author + " = " + quantity);
                    added++;
                }
            }

            if (entries.isEmpty()) {
                entries = db.getOfflineSampleBooks();
                entries.add(0, "⚠️ No results online. Using offline samples.");
            }

        } catch (Exception e) {
            entries = db.getOfflineSampleBooks();
            entries.add(0, "⚠️ Error loading online data: " + e.getMessage());
        }

        return entries;
    }



    private List<BookEntry> parseBookEntries(String text, Label feedback) {
        List<BookEntry> entries = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        int lineNumber = 1;

        for (String line : lines) {
            if (line.strip().isEmpty()) {
                lineNumber++;
                continue;
            }

            // Split on '=' to separate quantity
            String[] eqParts = line.trim().split("=", 2);
            if (eqParts.length != 2 || !eqParts[1].trim().matches("\\d+")) {
                feedback.setText("⚠️ Line " + lineNumber + " has invalid format. Use: title - author = quantity");
                return Collections.emptyList();
            }

            String quantityPart = eqParts[1].trim();
            int quantity = Integer.parseInt(quantityPart);

            // Split the left part on '-' to get title and author
            String[] dashParts = eqParts[0].trim().split("\\s*-\\s*", 2);
            if (dashParts.length != 2) {
                feedback.setText("⚠️ Line " + lineNumber + " has invalid format. Use: title - author = quantity");
                return Collections.emptyList();
            }

            String title = dashParts[0].trim();
            String author = dashParts[1].trim();

            if (title.isEmpty() || author.isEmpty() || quantity <= 0) {
                feedback.setText("⚠️ Line " + lineNumber + " is incomplete or invalid.");
                return Collections.emptyList();
            }

            entries.add(new BookEntry(title, author, quantity));
            lineNumber++;
        }

        feedback.setStyle("-fx-text-fill: green;");
        feedback.setText("✅ Books parsed successfully.");
        return entries;
    }

    private HBox createInventorySummaryBox() {
        Stream.of(registeredLabel, borrowedLabel, availableLabel).forEach(label -> {
            label.setStyle("""
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-background-color: #f0f0f0;
            -fx-padding: 8 14;
            -fx-background-radius: 8;
            -fx-border-color: #ccc;
            -fx-border-radius: 8;
        """);
        });

        HBox summaryBox = new HBox(20, registeredLabel, borrowedLabel, availableLabel);
        summaryBox.setAlignment(Pos.CENTER_LEFT);
        summaryBox.setPadding(new Insets(10, 0, 0, 0));
        return summaryBox;
    }

    private void updateInventorySummary() {
        int totalRegistered = books.stream().mapToInt(Book::getTotalQuantity).sum();
        int totalBorrowed = books.stream().mapToInt(Book::getBorrowed).sum();
        int totalAvailable = books.stream().mapToInt(Book::getAvailable).sum();

        registeredLabel.setText("📦 Registered: " + totalRegistered);
        borrowedLabel.setText("📚 Borrowed: " + totalBorrowed);
        availableLabel.setText("✅ Available: " + totalAvailable);
    }

}