package com.x.memories.models;

import java.io.Serializable;

/**
 * Created by AKINDE-PETERS on 12/1/2016.
 */

public class Event implements Serializable {

    private String id;
    private String name;
    private String description;
    private String createdby;
    private String time;
    private Boolean protect;
    private int code;


    public Event(String id, String name, String description, String createdby, String time, Boolean protect, int code) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdby = createdby;
        this.time = time;
        this.protect = protect;
        this.code = code;
    }

    public Event() {
    }

    public String getTime() {

        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getProtect() {
        return protect;
    }

    public void setProtect(Boolean protect) {
        this.protect = protect;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
