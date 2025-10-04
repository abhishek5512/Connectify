package com.connectify.connectify;

public class User {
    public String name, email, role;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
