package com.example.newstep.Models;


import com.google.firebase.Timestamp;

public class ReportPost {
  private String reportPostId,pcontent,postId,pusername;
    private int reportCount;
   private Timestamp lastReportPostTime;


    public String getPcontent() {
        return pcontent;
    }

    public void setPcontent(String pcontent) {
        this.pcontent = pcontent;
    }

    public String getPusername() {
        return pusername;
    }

    public void setPusername(String pusername) {
        this.pusername = pusername;
    }

    public Timestamp getLastReportPostTime() {
        return lastReportPostTime;
    }

    public String getReportPostId() {
        return reportPostId;
    }

    public void setReportPostId(String reportPostId) {
        this.reportPostId = reportPostId;
    }

    public void setLastReportPostTime(Timestamp lastReportPostTime) {
        this.lastReportPostTime = lastReportPostTime;
    }

    public int getReportCount() {
        return reportCount;
    }

    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }


}
