package com.example.newstep.Models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class ChatMsgModel {
    private String messageId;
    private String message;
    private String  senderId;
    private Timestamp timestamp;
    private int likeCount;
    private List<String> likedBy;
    private boolean isDateVisible = false;




    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    public ChatMsgModel() {
        this.likedBy = new ArrayList<>();
        this.likeCount = 0;
    }
    public ChatMsgModel(String message, String senderId, Timestamp timestamp) {
        this(message, senderId, timestamp, new ArrayList<>(), 0);
    }
    public ChatMsgModel(String message, String senderId, Timestamp timestamp , List<String> likedBy, int likeCount) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.likedBy = likedBy;
        this.likeCount = likeCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getLikedBy() {
        return likedBy != null ? likedBy : new ArrayList<>();
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy != null ? likedBy : new ArrayList<>();
        this.likeCount = this.likedBy.size();
    }

    public int getLikeCount() {
        return Math.max(likeCount, 0);
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = Math.max(likeCount, 0);
    }


    public boolean toggleLike(String userId) {
        if (likedBy == null) {
            likedBy = new ArrayList<>();
        }

        boolean isLiked = likedBy.contains(userId);
        if (isLiked) {
            likedBy.remove(userId);
            likeCount--;
        } else {
            likedBy.add(userId);
            likeCount++;
        }
        return !isLiked;
    }
    public boolean toggleDateVisibility() {
        isDateVisible = !isDateVisible;
        return isDateVisible;
    }

    public boolean isDateVisible() {
        return isDateVisible;
    }
}
