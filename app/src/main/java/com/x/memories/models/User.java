package com.x.memories.models;

/**
 * Created by AKINDE-PETERS on 8/31/2016.
 */
public class User {

    public User() {}

    public String name;
    public String username;
    public String email;
    public String uid;


    public User(String name, String uid, String email, String username) {
        this.name = name;
        this.uid = uid;
        this.email = email;
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
