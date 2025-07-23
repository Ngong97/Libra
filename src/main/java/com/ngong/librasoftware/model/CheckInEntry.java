package com.ngong.librasoftware.model;

public class CheckInEntry {
    private final int borrowingId;
    private final String bookTitle;
    private final String author;
    private final String dueDate;
    private final int bookId;
    private final String isbn;
    public CheckInEntry(int id, int bookId, String title, String author, String isbn,String due) {
        this.borrowingId = id;
        this.bookId = bookId;
        this.bookTitle = title;
        this.author = author;
        this.isbn = isbn;
        this.dueDate = due;

    }

    public int getBorrowingId() { return borrowingId; }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getAuthor() {
        return author;
    }

    public int getBookId() { return bookId; }

    public String getIsbn() { return isbn; }
    @Override
    public String toString() {
        return "• " + bookTitle + " — " + author + " (Due: " + dueDate + ")";
    }
}
