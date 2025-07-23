package com.ngong.librasoftware.view;

import com.ngong.librasoftware.DAO.DatabaseService;
import com.ngong.librasoftware.model.CheckInEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class CheckInPopup extends Stage {
    public CheckInPopup(int studentId, Pane contentArea) {
        setTitle("Return Books");
//        initOwner(this.getScene().getWindow());

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label heading = new Label("📚 Books Borrowed");
        ListView<CheckInEntry> bookList = new ListView<>();
        ObservableList<CheckInEntry> entries = FXCollections.observableArrayList(
                new DatabaseService().getBorrowedBooksForCheckIn(studentId)
        );
        bookList.setItems(entries);
        bookList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Button clear = new Button("✅ Mark as Returned");
        clear.setOnAction(e -> {
            ObservableList<CheckInEntry> selected = bookList.getSelectionModel().getSelectedItems();
            if (!selected.isEmpty()) {
                List<Integer> borrowingIds = selected.stream().map(CheckInEntry::getBorrowingId).toList();
                new DatabaseService().clearReturnedBooks(borrowingIds);

                // Refresh StudentsView if needed
                contentArea.getChildren().setAll(new StudentsView(contentArea));
                this.close();
            }
        });

        layout.getChildren().addAll(heading, bookList, clear);
        setScene(new Scene(layout, 500, 400));
        initModality(Modality.APPLICATION_MODAL);

    }

}
