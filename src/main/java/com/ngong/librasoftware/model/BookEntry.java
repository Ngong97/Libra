package com.ngong.librasoftware.model;

public class BookEntry {
    private final String title;
    private final String author;
    private final int quantity;

    public BookEntry(String title, String author, int quantity) {
        this.title = title;
        this.author = author;
        this.quantity = quantity;
    }

    public String title() {
        return title;
    }

    public String author() {
        return author;
    }

    public int quantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return title + " — " + author + " (" + quantity + " copies)";
    }

    public String toFormattedLine() {
        return title + " - " + author + " = " + quantity;
    }
}

