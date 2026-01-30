package com.ngong.librasoftware.model;

public class Student {
    public String name;
    public String id;
    public String gender;
    public String studentClass;

    public Student(String name, String id, String gender, String studentClass) {
        this.name = name != null ? name : "";
        this.id = id != null ? id : "";
        this.gender = gender != null ? gender : "";
        this.studentClass = studentClass != null ? studentClass : "";
    }
}
