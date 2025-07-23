package com.ngong.librasoftware.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BookProperty {
    private final StringProperty serialNum;
    private final StringProperty id;
    private final StringProperty  title;
    private final StringProperty author;
    private final StringProperty totalQuantity;
    private final StringProperty borrowed;
    private final StringProperty available;

    public BookProperty(int serialNum, int id, String title, String author, int totalQuantity, int borrowed, int available) {
        this.serialNum = new SimpleStringProperty(String.valueOf(serialNum));
        this.id = new SimpleStringProperty(String.valueOf(id));
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.totalQuantity = new SimpleStringProperty(String.valueOf(totalQuantity));
        this.borrowed = new SimpleStringProperty(String.valueOf(borrowed));
        this.available = new SimpleStringProperty(String.valueOf(available));
    }
    public StringProperty serialNumberProperty() { return serialNum; }
    public StringProperty idProperty() { return id; }
    public StringProperty titleProperty() { return title; }
    public StringProperty authorProperty() { return author; }
    public StringProperty quantityProperty() { return totalQuantity; }
    public StringProperty borrowedProperty() { return borrowed; }
    public StringProperty availableProperty() { return available; }


}
