package com.ngong.librasoftware.model;

public class BookInfo {
    private final String title;
    private final String author;

    public BookInfo(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
    // Getters for both
}

