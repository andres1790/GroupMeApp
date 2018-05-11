package com.andresdlg.groupmeapp.Entities;

public class Post {

    private String postId;
    private String text;
    private long time;
    private String userId;
    private String groupName;

    public Post(String postId, String text, long time, String userId, String groupName){
        this.postId = postId;
        this.text = text;
        this.time = time;
        this.userId = userId;
        this.groupName = groupName;
    }

    public Post(){ }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}