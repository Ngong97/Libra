package com.ngong.librasoftware.view;


import com.ngong.librasoftware.model.StudentRecord;
import com.ngong.librasoftware.utils.AnimationUtils;
import com.ngong.librasoftware.utils.UIUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;
import java.util.function.Function;

public class ClearedStudentsView extends BorderPane {

    private final TableView<StudentRecord> tableView = new TableView<>();
    private final TextField searchField = new TextField();

    public ClearedStudentsView(List<StudentRecord> clearedRecords, Pane contentPane) {
        setPadding(new Insets(20));
        setTop(buildHeader(contentPane));
        setCenter(buildTable(clearedRecords));


        this.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());


        AnimationUtils.applyFadeIn(this, 600);

    }

    private Node buildHeader(Pane contentArea) {
        Label title = new Label("Cleared Records");
        title.setFont(new Font("Matura MT Script Capitals", 20));

        searchField.setPromptText("Type search criteria...");
        searchField.getStyleClass().add("search-bar");
        searchField.setMinWidth(300);



        ImageView searchIcon = UIUtils.loadExternalImageView("/images/search.png",16,16);


        StackPane searchContainer = new StackPane(searchField);
        StackPane.setAlignment(searchIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(searchIcon, new Insets(0, 12, 0, 0)); // right padding
        searchContainer.getChildren().add(searchIcon);
        searchContainer.getStyleClass().add("search-container");




        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);


        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        ImageView undoIcon = UIUtils.loadExternalImageView("/images/undo.png",25,20);

        DropShadow blackShadow = new DropShadow();
        blackShadow.setOffsetY(2.0);
        blackShadow.setColor(Color.BLACK); // Set shadow color and transparency


        Hyperlink backBtn = new Hyperlink();
        backBtn.setGraphic(undoIcon);
        backBtn.setCursor(Cursor.HAND);
        Tooltip undTooltip=new Tooltip();
        undTooltip.setText("Go Back");
        backBtn.getStyleClass().add("button");
        backBtn.setTooltip(undTooltip);
        backBtn.setEffect(blackShadow);


        backBtn.setOnAction(e -> {
            contentArea.getChildren().setAll(new StudentsView(contentArea));
        });



        HBox header = new HBox(backBtn,spacer,title, spacer2, searchContainer);
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
                            record.getBookTitle().toLowerCase().contains(lower) ||
                            record.getStudentGender().toLowerCase().contains(lower) ||
                            record.getBookAuthor().toLowerCase().contains(lower) ||
                            record.getStudentName().toLowerCase().contains(lower)
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
        snCol.setMaxWidth(40);
        snCol.setSortable(false); // Optional: prevent sorting by serial number



        tableView.getColumns().addAll(
                snCol,
                createColumn("Name", StudentRecord::nameProperty,230),
                createColumn("Identity", StudentRecord::studentIdentityProperty,120),
                createColumn("Gender", StudentRecord::genderProperty,90),
                createColumn("Class", StudentRecord::studentClassProperty,90),
                createColumn("Term", StudentRecord::termProperty,80),
                createColumn("Borrow Date", StudentRecord::borrowDateProperty,100),
                createColumn("Return Date", StudentRecord::returnDateProperty,100),
                createColumn("Books", StudentRecord::bookTitlesProperty,200),
                createColumn("Authors", StudentRecord::authorsProperty,200)
        );

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return tableView;
    }

    private TableColumn<StudentRecord, String> createColumn(String title,
                                                            Function<StudentRecord, StringProperty> prop,double prefWidth) {
        TableColumn<StudentRecord, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> prop.apply(cellData.getValue()));
        col.setMaxWidth(prefWidth);
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
