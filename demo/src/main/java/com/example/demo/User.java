package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    private String user_id;

    private String name;
    private String email;
    private String phone;
    private String password_hash;

    @Enumerated(EnumType.STRING)
    private UserType type;

    // getters + setters
    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword_hash() { return password_hash; }
    public void setPassword_hash(String password_hash) { this.password_hash = password_hash; }

    public UserType getType() { return type; }
    public void setType(UserType type) { this.type = type; }
}
