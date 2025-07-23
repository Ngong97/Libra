package com.ngong.librasoftware.model;

public class OverdueStudentRecord {
    private final String studentName;
    private final String gender;
    private final String studentClass;
    private final String bookTitle;
    private final String author;
    private final String isbn;
    private final String borrowDate;
    private final String returnDate;

    public OverdueStudentRecord(String studentName, String gender, String studentClass,
                                String bookTitle, String author, String isbn,
                                String borrowDate, String returnDate) {
        this.studentName = studentName;
        this.gender = gender;
        this.studentClass = studentClass;
        this.bookTitle = bookTitle;
        this.author = author;
        this.isbn = isbn;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }

    public String getStudentName() { return studentName; }
    public String getGender() { return gender; }
    public String getStudentClass() { return studentClass; }
    public String getBookTitle() { return bookTitle; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public String getBorrowDate() { return borrowDate; }
    public String getReturnDate() { return returnDate; }
}
