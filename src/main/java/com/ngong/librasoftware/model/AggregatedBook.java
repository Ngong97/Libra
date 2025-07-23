package com.ngong.librasoftware.model;

public class AggregatedBook {
    public int bookId;
    public String title;
    public String author;
    public int totalQuantity;
    public int borrowed = 0;

    public AggregatedBook(int bookId, String title, String author, int totalQuantity) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.totalQuantity = totalQuantity;
    }
}