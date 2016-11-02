package com.x.memories.models;

import java.io.Serializable;

/**
 * Created by AKINDE-PETERS on 8/31/2016.
 */
public class Post implements Serializable {

    String url,caption, time, uid;
    Boolean privacy;

    public Boolean getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Boolean privacy) {
        this.privacy = privacy;
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

    public Post() {

    }

    public Post(String url, Boolean privacy, String uid, String time, String caption) {
        this.url = url;
        this.privacy = privacy;
        this.uid = uid;
        this.time = time;
        this.caption = caption;
    }
}
