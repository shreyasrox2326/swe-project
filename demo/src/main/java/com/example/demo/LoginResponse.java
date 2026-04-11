package com.example.demo;

public class LoginResponse {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String type;

    public LoginResponse() {
    }

    public LoginResponse(User user) {
        this.userId = user.getUser_id();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.type = user.getType().name();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
