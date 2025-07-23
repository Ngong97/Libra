package com.ngong.librasoftware.model;

import java.util.Map;

public class ReportData {
    private int registered;
    private int borrowed;
    private int remaining;
    private int notReturned;
    private Map<String, Integer> gender;
    private Map<String, Integer> classDistribution;

    public int getRegistered() { return registered; }
    public void setRegistered(int registered) { this.registered = registered; }

    public int getBorrowed() { return borrowed; }
    public void setBorrowed(int borrowed) { this.borrowed = borrowed; }

    public int getRemaining() { return remaining; }
    public void setRemaining(int remaining) { this.remaining = remaining; }

    public int getNotReturned() { return notReturned; }
    public void setNotReturned(int notReturned) { this.notReturned = notReturned; }

    public Map<String, Integer> getGender() { return gender; }
    public void setGender(Map<String, Integer> gender) { this.gender = gender; }

    public Map<String, Integer> getClassDistribution() { return classDistribution; }
    public void setClassDistribution(Map<String, Integer> classDistribution) { this.classDistribution = classDistribution; }
}
