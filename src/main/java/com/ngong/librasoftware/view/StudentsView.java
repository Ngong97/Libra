package com.ngong.librasoftware.view;


import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.model.SnackbarForRegistration;
import com.ngong.librasoftware.model.StudentRecord;
import com.ngong.librasoftware.utils.AnimationUtils;
import com.ngong.librasoftware.utils.UIUtils;
import com.ngong.librasoftware.utils.WarningMessage;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public class StudentsView extends VBox {
    StackPane root;
    SnackbarForRegistration snackbar;
    private final Map<Integer, Map<Integer, Integer>> lastClearedStudents = new HashMap<>();


    private boolean hasAnimatedRows = false;
    ComboBox<String> titleComboBox;
    private final Pane contentArea;
    private final DatabaseService db = new DatabaseService();
    private TableView<StudentRecord> table;

    public StudentsView(Pane contentArea) {
        this.setPadding(new Insets(20));
        this.setSpacing(10);
        this.contentArea = contentArea;


//        ImageView searchIcon = new ImageView(new Image(getClass().getResource("/images/search.png").toExternalForm()));
//          searchIcon.setFitWidth(16);
//          searchIcon.setFitHeight(16);
        ImageView searchIcon = UIUtils.loadExternalImageView("/images/search.png",16,16);


        TextField nameSearch = new TextField();
        nameSearch.setPromptText("Search records...");
        nameSearch.setMinWidth(300);
        nameSearch.getStyleClass().add("search-bar");

        CheckBox selectFilteredCheckbox = new CheckBox("Select All");

        // Wrap them in a styled container
        StackPane searchContainer = new StackPane(nameSearch);
        StackPane.setAlignment(searchIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(searchIcon, new Insets(0, 12, 0, 0)); // right padding
        searchContainer.getChildren().add(searchIcon);
        searchContainer.getStyleClass().add("search-container");


//        HBox rightSearch = new HBox(searchContainer);
//        rightSearch.setAlignment(Pos.CENTER_RIGHT);
//        rightSearch.setPadding(new Insets(10));
        HBox rightSearch = new HBox(10, searchContainer);
        rightSearch.setAlignment(Pos.CENTER_RIGHT);
        rightSearch.setPadding(new Insets(10));


        DropShadow blueShadow = new DropShadow();
        blueShadow.setOffsetY(2.0);
        blueShadow.setColor(Color.BLUE); // Set shadow color and transparency


        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency


//        ImageView undoIcon = new ImageView(new Image(getClass().getResource("/images/undo.png").toExternalForm()));
        ImageView undoIcon = UIUtils.loadExternalImageView("/images/undo.png",35,30);


        Hyperlink undoClearanceBtn = new Hyperlink();
        undoClearanceBtn.setGraphic(undoIcon);
        undoClearanceBtn.setCursor(Cursor.HAND);
        Tooltip undTooltip=new Tooltip();
        undTooltip.setText("Undo Mistaken Clearance");
        undoClearanceBtn.getStyleClass().add("button");
        undoClearanceBtn.setTooltip(undTooltip);
        undoClearanceBtn.setEffect(blackShadow);


        undoClearanceBtn.setOnAction(e -> {
            if (lastClearedStudents.isEmpty()) {
                WarningMessage msg = new WarningMessage(
                        "No recent clearance to undo!",
                        Duration.seconds(5),
                        contentArea
                );
                contentArea.getChildren().add(msg);
                StackPane.setAlignment(msg, Pos.CENTER);
                return;
            }

            for (int studentId : lastClearedStudents.keySet()) {
                Map<Integer, Integer> restoredMap = lastClearedStudents.get(studentId);
                db.restoreReturnStatusForBooks(studentId, restoredMap);
            }

            lastClearedStudents.clear();
            contentArea.getChildren().setAll(new StudentsView(contentArea));

            WarningMessage msg = new WarningMessage(
                    "🕘 Clearance undone successfully!",
                    Duration.seconds(4),
                    contentArea
            );
            contentArea.getChildren().add(msg);
            StackPane.setAlignment(msg, Pos.CENTER);
        });



        undoClearanceBtn.setOnMouseEntered(event -> {
            undoClearanceBtn.setScaleX(1.1);
//            copyTable.setCursor(Cursor.HAND);
            undoClearanceBtn.setEffect(blueShadow);
        });

        undoClearanceBtn.setOnMouseExited(event -> {
            undoClearanceBtn.setScaleX(1.0);
            undoClearanceBtn.setEffect(blackShadow);
        });
//        copyTable.setEffect(sd);
        undoClearanceBtn.setStyle("-fx-font-family: Consolas;" +      // Set font family
                "-fx-font-size: 14;" +
                "-fx-border-radius: 10");
//        ImageView inIcon = new ImageView(new Image(getClass().getResource("/images/book-in2.png").toExternalForm()));
        ImageView inIcon = UIUtils.loadExternalImageView("/images/book-in2.png",35,30);

        Hyperlink checkIn = new Hyperlink();
        checkIn.setGraphic(inIcon);
        checkIn.setCursor(Cursor.HAND);
        checkIn.getStyleClass().add("button");
        Tooltip checkinTooltip=new Tooltip("Receive a book from Student");
        Tooltip.install(checkIn, checkinTooltip);
//        checkinTooltip.setText("Receive a book from Student");
//        checkinTooltip.setStyle("-fx-");
//        checkIn.setTooltip(checkinTooltip);
        checkIn.setEffect(blackShadow);
        checkIn.setOnMouseEntered(event -> {
            checkIn.setScaleX(1.1);
//            copyTable.setCursor(Cursor.HAND);
            checkIn.setEffect(blueShadow);
        });

        checkIn.setOnMouseExited(event -> {
            checkIn.setScaleX(1.0);
            checkIn.setEffect(blackShadow);
        });
//        copyTable.setEffect(sd);
        checkIn.setStyle("-fx-font-family: Consolas;" +      // Set font family
                "-fx-font-size: 14;" +
                "-fx-border-radius: 10");
        checkIn.setOnAction(e -> {

            ObservableList<StudentRecord> selectedItems = table.getSelectionModel().getSelectedItems();
            if (selectedItems.isEmpty()){

                String hint = "Please select a student to clear!";

                WarningMessage msg = new WarningMessage(
                        hint,
                        Duration.seconds(5),
                        contentArea // Your layout container
                );

                contentArea.getChildren().add(msg);
                StackPane.setAlignment(msg, Pos.CENTER);
                return;
            }

            clearSelectedStudents();
            filterStudents(nameSearch,table);
        });


        // Left side: action buttons

        Hyperlink save = new Hyperlink();
//        ImageView download = new ImageView(new Image(getClass().getResource("/images/downloadIcon.png").toExternalForm()));
        ImageView download = UIUtils.loadExternalImageView("/images/downloadIcon.png",35,30);

        save.setGraphic(download);
        save.setCursor(Cursor.HAND);

        Tooltip saveTooltip=new Tooltip();
        saveTooltip.setText("Save Records to Excel file");
        save.setTooltip(saveTooltip);
        save.getStyleClass().add("button");
        save.setEffect(blackShadow);
        save.setOnMouseEntered(event -> {
            save.setScaleX(1.1);
//            copyTable.setCursor(Cursor.HAND);
            save.setEffect(blueShadow);
        });

        save.setOnMouseExited(event -> {
            save.setScaleX(1.0);
            save.setEffect(blackShadow);
        });
//        copyTable.setEffect(sd);
        save.setStyle("-fx-font-family: Consolas;" +      // Set font family
                "-fx-font-size: 14;" +
                "-fx-border-radius: 10");


        save.setOnAction(e -> {
            List<StudentRecord>someList=filteredStudents(nameSearch);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Excel File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook", ".xls"));


            String defaultFileName = "Overdue Students";
            File defaultDir = new File(System.getProperty("user.home"), "Documents");
            fileChooser.setInitialDirectory(defaultDir.exists() ? defaultDir : new File(System.getProperty("user.home")));
            fileChooser.setInitialFileName(defaultFileName + ".xls");




            File file = fileChooser.showSaveDialog(this.contentArea.getScene().getWindow());

//            boolean success=writeToExcel(someList, file);

            if (file != null) {
                try {
                    boolean success=writeToExcel(someList, file);
                    if (success) {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(file);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        });

        Hyperlink copyTable = new Hyperlink();
        copyTable.getStyleClass().add("button");
//        ImageView copy = new ImageView(new Image(getClass().getResource("/images/copyIcon.png").toExternalForm()));
        ImageView copy = UIUtils.loadExternalImageView("/images/copyIcon.png",35,30);

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

        copyTable.setOnMouseExited(event -> {copyTable.setScaleX(1.0);
            copyTable.setEffect(blackShadow);
        });
//        copyTable.setEffect(sd);
        copyTable.setStyle("-fx-font-family: Consolas;" +      // Set font family
                "-fx-font-size: 14;" +
                "-fx-border-radius: 10");

        copyTable.setOnAction(e -> {
            copyTableToClipboard();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);


        Label filteredNum=new Label();
        filteredNum.setStyle("-fx-font-size: 20;-fx-font-weight: bold;-fx-font-family: Consolas");

        filteredNum.setText(String.valueOf(filteredStudentsNum(nameSearch)));


        nameSearch.textProperty().addListener((obs, oldVal, newVal) ->  {
            filterStudents(nameSearch,table);
            filteredNum.setText(String.valueOf(filteredStudentsNum(nameSearch)));
            selectFilteredCheckbox.setSelected(false); // reset checkbox when filtering changes

        });

        // Checkbox logic: select/deselect visible rows
        selectFilteredCheckbox.setOnAction(e -> {
            if (selectFilteredCheckbox.isSelected()) {
                table.getSelectionModel().clearSelection();
                table.getItems().forEach(table.getSelectionModel()::select);
            } else {
                table.getSelectionModel().clearSelection();
            }
        });

        HBox leftActions = new HBox(10, undoClearanceBtn, checkIn,spacer,new Label(),save,copyTable,new Label(),new Label(),filteredNum,new Label(),selectFilteredCheckbox);
        leftActions.setAlignment(Pos.CENTER_LEFT);


// Combined header bar
        BorderPane headerBar = new BorderPane();
        headerBar.setLeft(leftActions);
        headerBar.setRight(rightSearch);
        headerBar.setPadding(new Insets(10, 10, 0, 10));




        table = new TableView<>();
//        CheckOutView.BookEntry2 book=new CheckOutView.BookEntry2();
        List<StudentRecord> students = db.getStudentBorrowingRecords();
        ObservableList<StudentRecord> tableData = FXCollections.observableArrayList();
        table.setItems(tableData);
        table.setCursor(Cursor.HAND);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        if (!hasAnimatedRows) {
            hasAnimatedRows = true;
            animateTableRows(students, tableData, Duration.millis(120));
        } else {
            tableData.setAll(students); // load instantly on future refreshes
        }

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(StudentRecord item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty && hasAnimatedRows) {
                    setOpacity(0);
                    FadeTransition ft = new FadeTransition(Duration.millis(400), this);
                    ft.setFromValue(0);
                    ft.setToValue(1);
                    ft.play();
                }
            }
        });

        table.getStyleClass().add("table-view");

        buildTableColumns();

        table.setRowFactory(tv -> {
            TableRow<StudentRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    StudentRecord selectedItem = row.getItem();
                    new EditStudentPopup(selectedItem, () -> {
                        contentArea.getChildren().setAll(new StudentsView(contentArea));
                    }).show();
                }
            });

            return row;
        });

//        filterStudents(nameSearch,classFilter,genderFilter,table);

        this.getStyleClass().add("students-view");
        this.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        this.getChildren().addAll(headerBar, table);


        Scene scene = contentArea.getScene();
        if (scene != null) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                    undoClearanceBtn.fire(); // simulate click
                    event.consume();
                }
            });
        }
        VBox.setVgrow(table, Priority.ALWAYS);
        AnimationUtils.applyFadeIn(this, 600);

    }

    private void copyTableToClipboard() {
        StringBuilder clipboardString = new StringBuilder();

        // Start the HTML table structure
        clipboardString.append("<html><body><table border='1'>");

        // Add the table headers
        clipboardString.append("<tr><th>S/N</th><th>Name</th><th>Gender</th><th>ID</th><th>Class</th><th>Term</th><th>Book Title</th><th>Author</th><th>Borrow Date</th><th>Return Date</th><th>Status</th></tr>");

        // Add each row of data
        for (StudentRecord result : table.getItems()) {
            clipboardString.append("<tr>")
                    .append("<td>").append(result.getSerialNum()).append("</td>")
                    .append("<td>").append(result.getStudentName()).append("</td>")
                    .append("<td>").append(result.getStudentGender()).append("</td>")
                    .append("<td>").append(result.getStudentId()).append("</td>")
                    .append("<td>").append(result.getStudentClass()).append("</td>")
                    .append("<td>").append(result.getTerm()).append("</td>")
                    .append("<td>").append(result.getBookTitle()).append("</td>")
                    .append("<td>").append(result.getBookAuthor()).append("</td>")
                    .append("<td>").append(result.getBorrowDate()).append("</td>")
                    .append("<td>").append(result.getReturnDate()).append("</td>")
                    .append("<td>").append(result.getStatus()).append("</td>")
                    .append("</tr>");
        }

        // Close the table and HTML tags
        clipboardString.append("</table></body></html>");

        // Create ClipboardContent with the HTML string
        ClipboardContent content = new ClipboardContent();
        content.putHtml(clipboardString.toString());  // Use the putHtml method to set HTML content
        Clipboard.getSystemClipboard().setContent(content);


        WarningMessage msg = new WarningMessage(
                "Table Copied to Clipboard!",
                Duration.seconds(5),
                contentArea // Your layout container
        );

        contentArea.getChildren().add(msg);
        StackPane.setAlignment(msg, Pos.CENTER);
    }

    public boolean writeToExcel(List<StudentRecord> records, File filePath) {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("BorrowingDetails");

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "S/N", "Student Name", "Gender", "ID", "Class",
                    "Book Titles", "Authors", "Borrow Date", "Return Date", "Status"
            };
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Populate data rows
            int rowNum = 1;
            for (StudentRecord record : records) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(record.getSerialNum());
                row.createCell(1).setCellValue(record.getStudentName());
                row.createCell(2).setCellValue(record.getStudentGender());
                row.createCell(3).setCellValue(record.getStudentId());
                row.createCell(4).setCellValue(record.getStudentClass());
                row.createCell(5).setCellValue(record.getBookTitle());
                row.createCell(6).setCellValue(record.getBookAuthor());

//                row.createCell(5).setCellValue(formatMultiline(record.getBookTitle()));
//                row.createCell(6).setCellValue(formatMultiline(record.getBookAuthor()));
                row.createCell(7).setCellValue(record.getBorrowDate());
                row.createCell(8).setCellValue(record.getReturnDate());
                row.createCell(9).setCellValue(record.getStatus());
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            workbook.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;

        }
    }

    private String formatMultiline(String value) {
        if (value == null || value.isBlank()) return "";
        return String.join("\n", value.split(",\\s*"));
    }

    private void animateTableRows(List<StudentRecord> students, ObservableList<StudentRecord> tableData, Duration delayPerRow) {
        Timeline timeline = new Timeline();

        for (int i = 0; i < students.size(); i++) {
            StudentRecord student = students.get(i);
            KeyFrame frame = new KeyFrame(delayPerRow.multiply(i), e -> {
                tableData.add(student);
            });
            timeline.getKeyFrames().add(frame);
        }

        timeline.play();
    }


    private void filterStudents(TextField nameSearch, TableView<StudentRecord> table) {
        String name = nameSearch.getText().trim().toLowerCase();
        List<StudentRecord> all = db.getStudentBorrowingRecords();
        List<StudentRecord> filtered = all.stream()
                .filter(s -> name.isEmpty() || s.getStudentName().toLowerCase().contains(name)||s.getBookAuthor().toLowerCase().contains(name)||s.getBookTitle().toLowerCase().contains(name)||s.getStudentId().toLowerCase().contains(name)||s.getStudentClass().toLowerCase().contains(name)||s.getStudentGender().toLowerCase().startsWith(name))
                .toList();

        table.setItems(FXCollections.observableArrayList(filtered));
    }


    private List<StudentRecord> filteredStudents(TextField nameSearch) {
        String name = nameSearch.getText().trim().toLowerCase();
        List<StudentRecord> all = db.getStudentBorrowingRecords();
        return all.stream()
                .filter(s -> name.isEmpty() || s.getStudentName().toLowerCase().contains(name)||s.getBookAuthor().toLowerCase().contains(name)||s.getBookTitle().toLowerCase().contains(name)||s.getStudentId().toLowerCase().contains(name)||s.getStudentClass().toLowerCase().contains(name)||s.getStudentGender().toLowerCase().startsWith(name)||s.getTerm().toLowerCase().startsWith(name))
                .toList();
    }


    private int filteredStudentsNum(TextField nameSearch) {
        String name = nameSearch.getText().trim().toLowerCase();
        List<StudentRecord> all = db.getStudentBorrowingRecords();
        return all.stream()
                .filter(s -> name.isEmpty() || s.getStudentName().toLowerCase().contains(name)||s.getBookAuthor().toLowerCase().contains(name)||s.getBookTitle().toLowerCase().contains(name)||s.getStudentId().toLowerCase().contains(name)||s.getStudentClass().toLowerCase().contains(name)||s.getStudentGender().toLowerCase().startsWith(name)||s.getTerm().toLowerCase().startsWith(name))
                .toList().size();
    }

    //...............................................


    private Map<Integer, Integer> showBookSelectionDialog(String studentName,
                                                          List<Integer> bookIds,
                                                          Map<Integer, String> bookIdTitleMap,
                                                          Map<Integer, Integer> borrowedQuantities) {
        Dialog<Map<Integer, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Books to Clear");
        dialog.initOwner(table.getScene().getWindow());
        dialog.setHeaderText("Select the book(s) and how many copies to clear for [" + studentName.toUpperCase() + "]");

        VBox bookItemsContainer = new VBox(10);
        Map<Integer, Spinner<Integer>> quantitySpinners = new HashMap<>();
        Map<Integer, CheckBox> selectionCheckboxes = new HashMap<>();

//        for (Integer bookId : bookIds) {
//            String title = bookIdTitleMap.get(bookId);
//            int borrowed = borrowedQuantities.getOrDefault(bookId, 1);
//
//            CheckBox selectBox = new CheckBox(title + " (Borrowed: " + borrowed + ")");
//            Spinner<Integer> spinner = new Spinner<>(0, borrowed, borrowed);
//            spinner.setEditable(true);
//            spinner.setDisable(true); // Start disabled
//
//            // Enable quantity input only if book is selected
//            selectBox.setOnAction(e -> spinner.setDisable(!selectBox.isSelected()));
//
//            selectionCheckboxes.put(bookId, selectBox);
//            quantitySpinners.put(bookId, spinner);
//
//            HBox row = new HBox(10, selectBox, spinner);
//            row.setAlignment(Pos.CENTER_LEFT);
//            bookItemsContainer.getChildren().add(row);
//        }

        for (Integer bookId : bookIds) {
            String title = bookIdTitleMap.get(bookId);
            int borrowed = borrowedQuantities.getOrDefault(bookId, 1);

            CheckBox selectBox = new CheckBox(title + " (Borrowed: " + borrowed + ")");
            HBox row;

            if (borrowed > 1) {
                Spinner<Integer> spinner = new Spinner<>(0, borrowed, borrowed);
                spinner.setEditable(true);
                spinner.setDisable(true);

                selectBox.setOnAction(e -> spinner.setDisable(!selectBox.isSelected()));

                quantitySpinners.put(bookId, spinner);
                selectionCheckboxes.put(bookId, selectBox);

                row = new HBox(10, selectBox, spinner);
            } else {
                selectionCheckboxes.put(bookId, selectBox);
                quantitySpinners.put(bookId, new Spinner<>(0, 1, 1)); // hidden, placeholder

                row = new HBox(10, selectBox); // ✅ Only checkbox if quantity == 1
            }

            row.setAlignment(Pos.CENTER_LEFT);
            bookItemsContainer.getChildren().add(row);
        }



        CheckBox selectAllCheckBox = new CheckBox("Select All");
        selectAllCheckBox.setOnAction(event -> {
            boolean selectAll = selectAllCheckBox.isSelected();
            selectionCheckboxes.forEach((id, checkbox) -> {
                checkbox.setSelected(selectAll);
                quantitySpinners.get(id).setDisable(!selectAll);
            });
        });

        VBox content = new VBox(15, selectAllCheckBox, bookItemsContainer);
        dialog.getDialogPane().setContent(content);
        dialog.setHeight(400);

        ButtonType buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, buttonTypeCancel);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOk) {
                Map<Integer, Integer> resultMap = new HashMap<>();
                selectionCheckboxes.forEach((bookId, checkbox) -> {
                    if (checkbox.isSelected()) {
                        int qtyToClear = quantitySpinners.get(bookId).getValue();
                        if (qtyToClear > 0) {
                            resultMap.put(bookId, qtyToClear);
                        }
                    }
                });
                return resultMap;
            }
            return null;
        });

        Optional<Map<Integer, Integer>> result = dialog.showAndWait();
        return result.orElse(Collections.emptyMap());
    }



//    private void clearReturnedStatus(String studentName) {
//        int exactId = db.getStudentIdWithUnreturnedBooks(studentName);
//        List<Integer> bookIds = db.getBookIdsForStudent(exactId);
//
//        if (bookIds.isEmpty()) {
//            showAlert("Error", "No books borrowed by " + studentName + " found.");
//            return;
//        }
//
//        Map<Integer, Integer> borrowedQuantities = db.getBorrowedQuantities(exactId, bookIds);
//
//        Map<Integer, Integer> selectedClearances = showBookSelectionDialog(
//                studentName,
//                bookIds,
//                db.getBookTitlesForIds(bookIds),
//                borrowedQuantities
//        );
//
//        if (!selectedClearances.isEmpty()) {
//            db.clearReturnStatusForBooks(exactId, selectedClearances);
//            lastClearedStudents.put(exactId, new HashMap<>(selectedClearances));
//        }
//    }

    private void clearReturnedStatus(String studentName) {
        int exactId = db.getStudentIdWithUnreturnedBooks(studentName);
        List<Integer> bookIds = db.getBookIdsForStudent(exactId);

        if (bookIds.isEmpty()) {
            showAlert("Error", "No books borrowed by " + studentName + " found.");
            return;
        }

        Map<Integer, Integer> borrowedQuantities = db.getBorrowedQuantities(exactId, bookIds);

        if (bookIds.size() == 1) {
            int bookId = bookIds.get(0);
            int quantity = borrowedQuantities.getOrDefault(bookId, 1);

            if (quantity == 1) {
                // ✅ Safe to auto-clear
                Map<Integer, Integer> autoClearMap = new HashMap<>();
                autoClearMap.put(bookId, 1);

                db.clearReturnStatusForBooks(exactId, autoClearMap);
                lastClearedStudents.put(exactId, new HashMap<>(autoClearMap));

                WarningMessage msg = new WarningMessage(
                        "Cleared successfully!",
                        Duration.seconds(5),
                        contentArea
                );
                contentArea.getChildren().add(msg);
                StackPane.setAlignment(msg, Pos.CENTER);
                return;
            }

            // ❗Book has multiple quantities — show popup anyway
        }

        // Multiple books OR one book with >1 quantity
        Map<Integer, Integer> selectedClearances = showBookSelectionDialog(
                studentName,
                bookIds,
                db.getBookTitlesForIds(bookIds),
                borrowedQuantities
        );

        if (!selectedClearances.isEmpty()) {
            db.clearReturnStatusForBooks(exactId, selectedClearances);
            WarningMessage msg = new WarningMessage(
                    "Cleared successfully!",
                    Duration.seconds(5),
                    contentArea
            );
            contentArea.getChildren().add(msg);
            StackPane.setAlignment(msg, Pos.CENTER);
            lastClearedStudents.put(exactId, new HashMap<>(selectedClearances));
        }
    }



    private void clearSelectedStudents() {
        ObservableList<StudentRecord> selectedItems = table.getSelectionModel().getSelectedItems();
            for (StudentRecord selectedItem : selectedItems) {
                clearReturnedStatus(selectedItem.getStudentName());
            }
    }

    private <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }


    //...............................................


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void buildTableColumns() {
        table.getColumns().addAll(
                createColumn("S/N", StudentRecord::serialNumberProperty, 40),
                createColumn("Name", StudentRecord::nameProperty, 270),
                createColumn("Gender", StudentRecord::genderProperty, 100),
                createColumn("ID", StudentRecord::studentIdentityProperty, 100),
                createColumn("Class", StudentRecord::studentClassProperty, 110),
                createColumn("Book Titles", StudentRecord::bookTitlesProperty, 280),
                createColumn("Authors", StudentRecord::authorsProperty, 270),
//                createColumn("Borrow Date", StudentRecord::borrowDateProperty, 60),
//                createColumn("Return Date", StudentRecord::returnDateProperty, 60),
                createColumn("Status", StudentRecord::statusProperty, 80)
        );
    }

    private TableColumn<StudentRecord, String> createColumn(String title,
                                                            Function<StudentRecord, ObservableValue<String>> mapper,
                                                            double prefWidth) {
        TableColumn<StudentRecord, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> mapper.apply(cellData.getValue()));
        col.setPrefWidth(prefWidth);
        return col;
    }


    private TableColumn<StudentRecord, String> createColumn(String title,
                                                            Function<StudentRecord, ObservableValue<String>> mapper) {
        TableColumn<StudentRecord, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> mapper.apply(cellData.getValue()));

        return col;
    }

    private void autoResizeColumns(TableView<?> table) {
        for (TableColumn<?, ?> column : table.getColumns()) {
            Text tempText = new Text(column.getText());
            double max = tempText.getLayoutBounds().getWidth();

            for (int i = 0; i < table.getItems().size(); i++) {
                Object cellData = column.getCellData(i);
                if (cellData != null) {
                    tempText = new Text(cellData.toString());
                    double width = tempText.getLayoutBounds().getWidth();
                    if (width > max) {
                        max = width;
                    }
                }
            }

            // Add padding and cap max width
            column.setPrefWidth(Math.min(max + 30, 300));
        }
    }

}
