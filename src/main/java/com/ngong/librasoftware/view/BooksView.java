package com.ngong.librasoftware.view;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.model.Book;
import com.ngong.librasoftware.model.BookEntry;
import com.ngong.librasoftware.utils.AnimationUtils;
import com.ngong.librasoftware.utils.WarningMessage;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
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
        title.getStyleClass().add("section-title");

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
        serialNumCol.setMaxWidth(50);
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



        ImageView addIcon = new ImageView(new Image(getClass().getResource("/images/book-out.png").toExternalForm()));
        addIcon.setFitWidth(30);
        addIcon.setFitHeight(30);

        Button addBooksBtn = new Button();
        addBooksBtn.getStyleClass().add("unique-button");
        addBooksBtn.setGraphic(addIcon);
        addBooksBtn.setEffect(blackShadow);

        addBooksBtn.setOnMouseEntered(event -> {
            addBooksBtn.setScaleX(1.1);
        });

        addBooksBtn.setOnMouseExited(event -> {
            addBooksBtn.setScaleX(1.0);
        });

        addBooksBtn.setOnAction(e -> showBulkAddDialog());




        ImageView delIcon = new ImageView(new Image(getClass().getResource("/images/deleteIcon.png").toExternalForm()));
        delIcon.setFitWidth(30);
        delIcon.setFitHeight(30);


        Hyperlink syncWithBorrowed = new Hyperlink("Sync With Borrowed");
        syncWithBorrowed.setOnAction(e -> showBulkAddDialogFromBorrowings());



//        Button deleteBtn = new Button("🗑️ Delete Selected");

        Button deleteBtn = new Button();
        deleteBtn.setOnAction(e -> deleteSelectedBooks(table));
        deleteBtn.setGraphic(delIcon);
        Tooltip deleteBtnTooltip=new Tooltip();
        deleteBtnTooltip.setText("Delete Selected");
//        deleteBtnTooltip.getStyleClass().add("nav-tooltip");
        deleteBtn.setTooltip(deleteBtnTooltip);

        deleteBtn.setEffect(blackShadow);

        deleteBtn.setOnMouseEntered(event -> {
            deleteBtn.setScaleX(1.1);
        });

        deleteBtn.setOnMouseExited(event -> {
            deleteBtn.setScaleX(1.0);
        });


        ImageView editIcon = new ImageView(new Image(getClass().getResource("/images/editIcon.png").toExternalForm()));
        editIcon.setFitWidth(30);
        editIcon.setFitHeight(30);



        Button editBtn = new Button();
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
        editBtn.setGraphic(editIcon);
        Tooltip editBtnTooltip=new Tooltip();
        editBtnTooltip.setText("Edit Selected");
//        editBtnTooltip.getStyleClass().add("nav-tooltip");
        editBtnTooltip.setShowDelay(Duration.millis(100));
        editBtnTooltip.setHideDelay(Duration.millis(100));
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
        ImageView copy = new ImageView(new Image(getClass().getResource("/images/copyIcon.png").toExternalForm()));
        copy.setFitWidth(30);
        copy.setFitHeight(30);
        copyTable.setGraphic(copy);
        copyTable.setCursor(Cursor.HAND);

        Tooltip copyTableTooltip=new Tooltip();
        copyTableTooltip.setText("Copy the table");
        copyTable.setTooltip(copyTableTooltip);

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

//        copyTable.setEffect(sd);
//        copyTable.setStyle("-fx-font-family: Consolas;" +      // Set font family
//                "-fx-font-size: 14;" +
//                "-fx-border-radius: 10");

        copyTable.setOnAction(e -> {
            copyTableToClipboard(table);
        });

        HBox buttonBar = new HBox(20,syncWithBorrowed, addBooksBtn,deleteBtn,editBtn,copyTable);
//        buttonBar.setSpacing(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);



        HBox summaryBox = createInventorySummaryBox();

        ImageView searchIcon = new ImageView(new Image(getClass().getResource("/images/search.png").toExternalForm()));
        searchIcon.setFitHeight(16);
        searchIcon.setFitWidth(16);

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

//        this.getChildren().addAll(title, search, summaryBox, table, buttonBar);
        this.getChildren().addAll(title,headerBar, table, buttonBar);
        AnimationUtils.applyFadeIn(this, 600);
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
            coverImage = new Image(getClass().getResource(imagePath).toExternalForm());
        } catch (Exception e) {
            coverImage = new Image(getClass().getResource("/images/covers/default_cover.png").toExternalForm());
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
//        double mouseX = event.getScreenX();
//        double mouseY = event.getScreenY();
//
//        double offsetX = 15; // rightward offset
//        double offsetY = 10; // upward offset
//
//        double popupWidth = bookHoverContent.getPrefWidth();
//        double popupHeight = bookHoverContent.prefHeight(-1); // estimates height
//
//        // Position so that bottom-left of popup is at top-right of pointer
//        double popupX = mouseX + offsetX;
//        double popupY = mouseY - popupHeight - offsetY;

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
                "Table copied to Clipboard successfully!",
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

        // TextArea for entries
        TextArea input = new TextArea();
        input.setStyle("-fx-font-size: 16; -fx-line-spacing: 8;");
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
                        case 0 -> transformed = capitalizeWords(selectedText);
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
        toolsDropdown.setStyle("-fx-font-size: 14;-fx-background-color: #c1b8b8;-fx-font-weight: bold");
        Tooltip.install(toolsDropdown, new Tooltip("Change case"));



        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK);

        Button pasteClipboard = new Button();
        Image pasteIcon = new Image(getClass().getResource("/images/pasteClipboard.png").toString()); // Create a BackgroundImage with the loaded image
        ImageView pasteIconView = new ImageView(pasteIcon);
        pasteIconView.setFitWidth(20);
        pasteIconView.setFitHeight(20);
        pasteClipboard.setGraphic(pasteIconView);
        Tooltip pastetooltip = new Tooltip("Paste (Ctrl+V)");
//        pastetooltip.getStyleClass().add("nav-tooltip");
        pastetooltip.setShowDelay(Duration.millis(100));
        pastetooltip.setHideDelay(Duration.millis(100));
        Tooltip.install(pasteClipboard, pastetooltip);
//        pasteClipboard.getStyleClass().add("Button-part");
        pasteClipboard.setCursor(Cursor.HAND);
        pasteClipboard.setEffect(blackShadow);

        Button clearAll = new Button();
        Image refreshIcon = new Image(getClass().getResource("/images/clear2.png").toString()); // Create a BackgroundImage with the loaded image
        ImageView refreshIconView = new ImageView(refreshIcon);
        refreshIconView.setFitWidth(20);
        refreshIconView.setFitHeight(20);
        clearAll.setGraphic(refreshIconView);
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
        Node cancelButton = dialog.getDialogPane().lookupButton(cancelButtonType);
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
                    "Please select one or more books to delete!",
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



    private void showEditDialog(Book book) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Book");
        dialog.getDialogPane().setPrefWidth(420);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField(book.getTitle());
        TextField authorField = new TextField(book.getAuthor());
        Spinner<Integer> totalField = new Spinner<>(1, 10000, book.getTotalQuantity());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.addRow(0, new Label("Title:"), titleField);
        form.addRow(1, new Label("Author:"), authorField);
        form.addRow(2, new Label("Total Quantity:"), totalField);

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String newTitle = titleField.getText().trim();
                String newAuthor = authorField.getText().trim();
                int newTotal = totalField.getValue();

                if (!newTitle.isEmpty() && !newAuthor.isEmpty()) {
                    db.updateLibraryBook(book.getId(), newTitle, newAuthor, newTotal);
                    books.setAll(db.getAllLibraryBooksWithAvailability());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }



    private void showBulkAddDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Register New Books");
        dialog.getDialogPane().setPrefWidth(500);

        // Text input area
        TextArea input = new TextArea();
        input.setStyle("-fx-font-size: 16;-fx-line-spacing: 8;");
        input.setPromptText("e.g.\nThings Fall Apart - Chinua Achebe = 5\nAtomic Habits - James Clear = 3");

        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.setStyle("-fx-text-fill: red;");

        // Manual category input
        TextField categoryInput = new TextField();
        categoryInput.setPromptText("Type category or select below...");
        categoryInput.setPrefWidth(240);

        // Predefined category dropdown
        ChoiceBox<String> categoryBox = new ChoiceBox<>();
        categoryBox.getItems().addAll("fiction", "science", "history", "biography", "technology", "philosophy", "education");
        categoryBox.setValue("fiction");

        // Keep both in sync
        categoryBox.setOnAction(e -> categoryInput.setText(categoryBox.getValue()));

        // Load Samples Online button
        Button loadSamplesBtn = new Button("Load Samples Online");
        loadSamplesBtn.setOnAction(e -> {
            input.setText("⏳ Loading...");
            String category = categoryInput.getText().isBlank() ? "fiction" : categoryInput.getText();

            new Thread(() -> {
                List<String> samples = generateSampleBookEntriesFromAPI(category);
                Platform.runLater(() -> input.setText(String.join("\n", samples)));
            }).start();
        });


        Button importBtn = new Button("📁 Load From File");
//        importBtn.setOnAction(e -> {
//            FileChooser chooser = new FileChooser();
//            chooser.setTitle("Import Book List");
//            chooser.getExtensionFilters().addAll(
//                    new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.csv")
//                    
//            );
//            File file = chooser.showOpenDialog(dialog.getOwner());
//            if (file != null) {
//                try {
//                    String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
//                    input.setText(content);
//                } catch (IOException ex) {
//                    feedback.setText("❌ Failed to read file: " + ex.getMessage());
//                }
//            }
//        });


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
        Node cancelButton = dialog.getDialogPane().lookupButton(cancelButtonType);

        // Buttons row
        HBox buttonRow = new HBox(10, loadSamplesBtn,importBtn, okButton, cancelButton);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        // Assemble the layout
        VBox content = new VBox(10,
                new Label("📝 Format: Title - Author = Quantity"),
                new HBox(10, new Label("Category:"), categoryInput, categoryBox),
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


//                                WarningMessage popup = new WarningMessage(
//                                        "📡 Connect to internet to load book descriptions!",
//                                        Duration.seconds(4),
//                                        contentArea
//                                );
//                                contentArea.getChildren().add(popup);
//                                StackPane.setAlignment(popup, Pos.CENTER);
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
        dialog.getDialogPane().setPrefWidth(500);

        TextArea input = new TextArea();
        input.setStyle("-fx-font-size: 16;-fx-line-spacing: 8;");
        input.setPromptText("e.g.\nThings Fall Apart - Chinua Achebe = 5");

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
        Node cancelButton = dialog.getDialogPane().lookupButton(cancelButtonType);
        HBox buttonRow = new HBox(10, okButton, cancelButton);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(10,
                new Label("📝 Review and edit before adding to inventory:"),
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



    private void importFromFile(File file) {
    Label feedback = new Label(); // Temporary feedback label
    try {
        String content = Files.readString(file.toPath());
        List<BookEntry> entries = parseBookEntries(content, feedback);

        if (entries.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Import Failed");
            alert.setHeaderText("No valid books found in file.");
            alert.setContentText(feedback.getText());
            alert.showAndWait();
            return;
        }

        for (BookEntry entry : entries) {
            db.addLibraryBookIfMissing(entry.title(), entry.author(), entry.quantity());
        }

        books.setAll(db.getAllLibraryBooksWithAvailability());

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Import Complete");
        success.setHeaderText(null);
        success.setContentText("📘 " + entries.size() + " book(s) successfully added.");
        success.showAndWait();

        updateInventorySummary();

    } catch (IOException e) {
        e.printStackTrace();
        Alert fail = new Alert(Alert.AlertType.ERROR);
        fail.setTitle("Import Error");
        fail.setHeaderText("Unable to read file");
        fail.setContentText("An error occurred while reading the file:\n" + e.getMessage());
        fail.showAndWait();
    }
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