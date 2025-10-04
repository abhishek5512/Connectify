package com.connectify.connectify.models;

import java.util.List;

public class Match {
    private String seekerEmail;
    private String seekerName;
    private List<String> seekerSkills;
    private String seekerAge;
    private String seekerQualification;
    private String seekerProfileImage;
    private String jobTitle;
    private String company;
    private String location;

    public Match() {
        // Required empty constructor for Firebase
    }

    public Match(String seekerEmail, String seekerName, List<String> seekerSkills, String seekerAge,
                 String seekerQualification, String seekerProfileImage, String jobTitle, String company, String location) {
        this.seekerEmail = seekerEmail;
        this.seekerName = seekerName;
        this.seekerSkills = seekerSkills;
        this.seekerAge = seekerAge;
        this.seekerQualification = seekerQualification;
        this.seekerProfileImage = seekerProfileImage;
        this.jobTitle = jobTitle;
        this.company = company;
        this.location = location;
    }

    // Getters
    public String getSeekerEmail() {
        return seekerEmail;
    }

    public String getSeekerName() {
        return seekerName;
    }

    public List<String> getSeekerSkills() {
        return seekerSkills;
    }

    public String getSeekerAge() {
        return seekerAge;
    }

    public String getSeekerQualification() {
        return seekerQualification;
    }

    public String getSeekerProfileImage() {
        return seekerProfileImage;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public String getLocation() {
        return location;
    }

    // ✅ Setter Methods (Fixing Missing Methods)
    public void setSeekerName(String seekerName) {
        this.seekerName = seekerName;
    }

    public void setSeekerSkills(List<String> seekerSkills) {
        this.seekerSkills = seekerSkills;
    }

    public void setSeekerAge(String seekerAge) {
        this.seekerAge = seekerAge;
    }

    public void setSeekerQualification(String seekerQualification) {
        this.seekerQualification = seekerQualification;
    }

    public void setSeekerProfileImage(String seekerProfileImage) {
        this.seekerProfileImage = seekerProfileImage;
    }
}
