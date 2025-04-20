package com.example.newstep.Models;

import com.google.firebase.Timestamp;

import java.util.List;

public class PostModel {
    private String category;
    private String id;
    private String userName;
    private String content,userId;
    private Timestamp timestamp;
    private int likes;
    private int dislikes;
    private List<String> likedBy;
    private List<String> dislikedBy;
    private String profileImageUrl;

    public PostModel() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public PostModel(String id,String userId, String content, int likes, String userName, int dislikes, Timestamp timestamp,String profileImageUrl,String category) {
        this.id = id;
        this.content = content;
        this.userName=userName;
        this.likes = likes;
        this.dislikes = dislikes;
        this.timestamp= timestamp;
        this.profileImageUrl = profileImageUrl;
        this.userId=userId;
        this.category=category;
    }

    public String getId() {
        return id;
    }
    public String getContent() {
        return content;
    }

    public int getLikes() {
        return likes; }
    public int getDislikes() {
        return dislikes; }

    public void setId(String id) {
        this.id = id; }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setLikes(int likes) { this.likes = likes;
    }
    public void setDislikes(int dislikes) { this.dislikes = dislikes;
    }
    public List<String> getDislikedBy() {
        return dislikedBy;
    }

    public void setDislikedBy(List<String> dislikedBy) {
        this.dislikedBy = dislikedBy;
    }

    public List<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getProfileImageUrl() {return profileImageUrl;}
}
