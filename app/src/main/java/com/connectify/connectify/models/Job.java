package com.connectify.connectify.models;

public class Job {
    private String jobId;
    private String title;
    private String company;
    private String salary;
    private String description;
    private String location;
    private String jobType;
    private String employerEmail;
    private String postedBy;
    private String companyLogoUrl; // ✅ New Field for Company Logo

    public Job() {}

    public Job(String title, String company, String salary, String description,
               String location, String jobType, String employerEmail, String companyLogoUrl) {
        this.title = title;
        this.company = company;
        this.salary = salary;
        this.description = description;
        this.location = location;
        this.jobType = jobType;
        this.employerEmail = employerEmail;
        this.companyLogoUrl = companyLogoUrl;
        this.postedBy = employerEmail;
    }

    // ✅ GETTERS & SETTERS
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getEmployerEmail() {
        return employerEmail;
    }

    public void setEmployerEmail(String employerEmail) {
        this.employerEmail = employerEmail;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public String getCompanyLogoUrl() {
        return companyLogoUrl;
    }

    public void setCompanyLogoUrl(String companyLogoUrl) {
        this.companyLogoUrl = companyLogoUrl;
    }
}
