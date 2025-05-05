package com.example.functioninglogin.LoginUI;

public class HelperClass {

    private String uid; // Firebase Auth UID
    private String name;
    private String email;

    // Constructor with all fields
    public HelperClass(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }

    // Default Constructor (Needed for Firebase)
    public HelperClass() {
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
