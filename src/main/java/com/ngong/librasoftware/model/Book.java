package com.ngong.librasoftware.model;

public class Book {
    private int serialNum;
    private int id;
    private String title;
    private String author;
    private int totalQuantity;
    private int borrowed;
    private int available;

    public Book(int serialNum,int id, String title, String author, int totalQuantity, int borrowed, int available) {
        this.serialNum = serialNum;
        this.id = id;
        this.title = title;
        this.author = author;
        this.totalQuantity = totalQuantity;
        this.borrowed = borrowed;
        this.available = available;
    }
    public int getSerialNum() { return serialNum; }
    public int getId() { return id; }
    public String getTitle() { return title != null ? title : ""; }
    public String getAuthor() { return author != null ? author : ""; }
    public int getTotalQuantity() { return totalQuantity; }
    public int getBorrowed() { return borrowed; }
    public int getAvailable() { return available; }
}
