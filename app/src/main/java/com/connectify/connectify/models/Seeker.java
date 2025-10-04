package com.connectify.connectify.models;

public class Seeker {
    private String name;
    private String email;
    private String skills;
    private String profileImage;

    // Default constructor required for Firestore
    public Seeker() {}

    public Seeker(String name, String email, String skills, String profileImage) {
        this.name = name;
        this.email = email;
        this.skills = skills;
        this.profileImage = profileImage;
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

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
