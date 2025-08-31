package com.ngong.librasoftware.view;


import com.ngong.librasoftware.model.StudentRecord;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.List;
import java.util.function.Function;

public class ClearedStudentsView extends BorderPane {

    private final TableView<StudentRecord> tableView = new TableView<>();
    private final TextField searchField = new TextField();

    public ClearedStudentsView(List<StudentRecord> clearedRecords, Pane contentPane) {
        setPadding(new Insets(20));
        setTop(buildHeader());
        setCenter(buildTable(clearedRecords));
    }

    private Node buildHeader() {
        Label title = new Label("Cleared Students");
        title.setFont(new Font("Arial", 20));

        searchField.setPromptText("Search by name, identity, class, term, or book...");
        searchField.setMaxWidth(300);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(title, spacer, searchField);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        header.setPadding(new Insets(10, 0, 20, 0));
        return header;
    }

    private Node buildTable(List<StudentRecord> records) {
        ObservableList<StudentRecord> data = FXCollections.observableArrayList(records);
        FilteredList<StudentRecord> filteredData = new FilteredList<>(data, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();
            filteredData.setPredicate(record ->
                    record.getStudentName().toLowerCase().contains(lower) ||
                            record.getIdentity().toLowerCase().contains(lower) ||
                            record.getStudentClass().toLowerCase().contains(lower) ||
                            record.getTerm().toLowerCase().contains(lower) ||
                            record.getBookTitle().toLowerCase().contains(lower)
            );
        });

        SortedList<StudentRecord> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);

        TableColumn<StudentRecord, String> snCol = new TableColumn<>("S/N");
        snCol.setCellValueFactory(cellData -> {
            int index = tableView.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(index));
        });
        snCol.setMaxWidth(50);
        snCol.setSortable(false); // Optional: prevent sorting by serial number



        tableView.getColumns().addAll(
                snCol,
                createColumn("Name", StudentRecord::nameProperty),
                createColumn("Identity", StudentRecord::studentIdentityProperty),
                createColumn("Class", StudentRecord::studentClassProperty),
                createColumn("Term", StudentRecord::termProperty),
                createColumn("Borrow Date", StudentRecord::borrowDateProperty),
                createColumn("Return Date", StudentRecord::returnDateProperty),
                createColumn("Books", StudentRecord::bookTitlesProperty),
                createColumn("Authors", StudentRecord::authorsProperty)
        );

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return tableView;
    }

    private TableColumn<StudentRecord, String> createColumn(String title,
                                                            Function<StudentRecord, StringProperty> prop) {
        TableColumn<StudentRecord, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> prop.apply(cellData.getValue()));
        return col;
    }

    // Optional: method to launch this view in a standalone scene
//    public static void showInStage(List<StudentRecord> clearedRecords) {
//        ClearedStudentsView view = new ClearedStudentsView(clearedRecords);
//        Scene scene = new Scene(view, 1000, 600);
//        Stage stage = new Stage();
//        stage.setTitle("Cleared Students");
//        stage.setScene(scene);
//        stage.show();
//    }
}
