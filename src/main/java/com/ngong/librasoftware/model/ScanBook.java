package com.ngong.librasoftware.model;

public class ScanBook {
    public String title;
    public String author;
    public String isbn;
    public int quantity;

    public ScanBook(String title, String author, String isbn) {
        this.title = title != null ? title : "";
        this.author = author != null ? author : "";
        this.isbn = isbn != null ? isbn : "";
        this.quantity = 1;
    }
}
