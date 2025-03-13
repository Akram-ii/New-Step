package com.example.newstep.Models;
import java.util.ArrayList;
import java.util.List;

public class Comment {
    private String commentId;
    private String userId;
    private String text;
    private long timestamp;
    private String postId;
    private String username;
    private List<String> likes;


    public Comment() {
        this.likes = new ArrayList<>();
    }


    public Comment(String commentId, String userId, String postId, String text, Long timestamp, String username) {
        this.commentId = commentId;
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;
        this.likes = new ArrayList<>();
        this.postId = postId;
        this.username = username;

    }

    public String getCommentId() {
        return commentId;
    }

    public String getUserId() {
        return userId;
    }

    public String getPostId() {
        return postId;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<String> getLikes() {
        return likes;
    }

    public String getUsername() {
        return username;
    }


    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}