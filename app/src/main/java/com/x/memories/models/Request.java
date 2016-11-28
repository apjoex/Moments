package com.x.memories.models;

import java.io.Serializable;

/**
 * Created by AKINDE-PETERS on 9/4/2016.
 */
public class Request implements Serializable {

    public String getBitmapString() {
        return bitmapString;
    }

    public void setBitmapString(String bitmapString) {
        this.bitmapString = bitmapString;
    }

    public Request(String name, String uid, String time, String post_type, String status, String post_id, String caption, String bitmapString) {
        this.name = name;
        this.uid = uid;
        this.time = time;
        this.post_type = post_type;
        this.status = status;
        this.post_id = post_id;
        this.caption = caption;
        this.bitmapString = bitmapString;

    }

    public Request() {
    }

    public String name;
    public String post_id;
    public String post_type;
    public String status;
    public String time;
    public String uid;
    public String bitmapString;

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String caption;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPost_type() {
        return post_type;
    }

    public void setPost_type(String post_type) {
        this.post_type = post_type;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }
}
