package com.x.memories.models;

/**
 * Created by apjoe on 12/23/2016.
 */

public class EventPost {
    private String url;
    private String caption;
    private String time;
    private String uid;
    private String event_id;


    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public EventPost() {

    }

    public EventPost(String url, String event_id, String uid, String time, String caption) {
        this.url = url;
        this.event_id = event_id;
        this.uid = uid;
        this.time = time;
        this.caption = caption;
    }
}
