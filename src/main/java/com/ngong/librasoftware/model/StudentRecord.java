package com.ngong.librasoftware.model;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StudentRecord {
    private final StringProperty serialNumber;
    public final StringProperty name;
    private final StringProperty identity;
    private final StringProperty studentId;
    private final StringProperty gender;
    private final StringProperty studentClass;
    private final StringProperty term;
    private final StringProperty bookTitles;
    private final StringProperty authors;
    private final StringProperty isbns;
    private final StringProperty status;
    private final StringProperty borrowDate;
    private final StringProperty returnDate;

    public StudentRecord(int sn, String name, String identity, int id, String gender,
                         String studentClass, String term,
                         String bookTitles, String authors,
                         String isbns, String status, String borrowDate, String returnDate) {
        this.serialNumber = new SimpleStringProperty(String.valueOf(sn));
        this.name = new SimpleStringProperty(name);
        this.identity = new SimpleStringProperty(identity);
        this.studentId = new SimpleStringProperty(String.valueOf(id));
        this.gender = new SimpleStringProperty(gender);
        this.studentClass = new SimpleStringProperty(studentClass);
        this.term = new SimpleStringProperty(term);
        this.bookTitles = new SimpleStringProperty(bookTitles);
        this.authors = new SimpleStringProperty(authors);
        this.isbns = new SimpleStringProperty(isbns);
        this.status = new SimpleStringProperty(status);
        this.borrowDate = new SimpleStringProperty(borrowDate);
        this.returnDate = new SimpleStringProperty(returnDate);


    }

    public StringProperty serialNumberProperty() { return serialNumber; }
    public StringProperty nameProperty() { return name; }
    public StringProperty studentIdentityProperty() { return identity; }
    public StringProperty studentIdProperty() { return studentId; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty studentClassProperty() { return studentClass; }
    public StringProperty termProperty() { return term; }
    public StringProperty bookTitlesProperty() { return bookTitles; }
    public StringProperty authorsProperty() { return authors; }
    public StringProperty isbnsProperty() { return isbns; }
    public StringProperty statusProperty() { return status; }
    public StringProperty borrowDateProperty() { return borrowDate; }
    public StringProperty returnDateProperty() { return returnDate; }


    public String getIsbn() {
        return isbns.get();
    }
    public String getIdentity() {return identity.get();}

    public String getStudentId() {return studentId.get();}
    public String getBookTitle() {
        return bookTitles.get();
    }
    public String getBookAuthor() {return authors.get();}
    public String getBorrowDate() {return borrowDate.get();}
    public String getReturnDate() {return borrowDate.get();}
    public String getSerialNum() {return serialNumber.get();}
    public String getTerm() {return term.get();}
    public String getStatus() {return status.get();}
    public String getStudentName() {
        return name.get();
    }
    public String getStudentGender() {
        return gender.get();
    }
    public String getStudentClass() {
        return studentClass.get();
    }
}
