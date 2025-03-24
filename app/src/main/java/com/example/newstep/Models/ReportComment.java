package com.example.newstep.Models;

import com.google.firebase.Timestamp;

import java.util.List;

public class ReportComment {
    private String id,postId,commentId,pUsername,pContent,cUsername,cContent;
    int nbReports;
    private Timestamp lastReportTime;
    private List<String> userIds;

    public String getcUsername() {
        return cUsername;
    }

    public void setcUsername(String cUsername) {
        this.cUsername = cUsername;
    }

    public String getcContent() {
        return cContent;
    }

    public void setcContent(String cContent) {
        this.cContent = cContent;
    }

    public String getpUsername() {
        return pUsername;
    }

    public void setpUsername(String pUsername) {
        this.pUsername = pUsername;
    }

    public String getpContent() {
        return pContent;
    }

    public void setpContent(String pContent) {
        this.pContent = pContent;
    }

    public int getNbReports() {
        return nbReports;
    }

    public void setNbReports(int nbReports) {
        this.nbReports = nbReports;
    }

    public ReportComment(String id, String postId, String commentId, Timestamp lastReportTime, List<String> userIds) {
        this.id = id;
        this.postId = postId;
        this.commentId = commentId;
        this.lastReportTime = lastReportTime;
        this.userIds = userIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public Timestamp getLastReportTime() {
        return lastReportTime;
    }

    public void setLastReportTime(Timestamp lastReportTime) {
        this.lastReportTime = lastReportTime;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
