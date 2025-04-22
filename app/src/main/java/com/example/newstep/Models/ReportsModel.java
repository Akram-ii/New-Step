package com.example.newstep.Models;

import com.google.firebase.Timestamp;

public class ReportsModel {

    private String  id ,RuserId,Rusername,Rtitle,Rdesc,Rcat;
    private Timestamp Rtime;

    public ReportsModel(String id, String ruserId, String rusername, String rtitle, String rdesc, String rcat, Timestamp rtime) {
        this.id = id;
        RuserId = ruserId;
        Rusername = rusername;
        Rtitle = rtitle;
        Rdesc = rdesc;
        Rcat = rcat;
        Rtime = rtime;
    }

    public String getId() {
        return id;
    }


    public String getRusername() {
        return Rusername;
    }



    public String getRtitle() {
        return Rtitle;
    }



    public String getRdesc() {
        return Rdesc;
    }



    public String getRcat() {
        return Rcat;
    }



    public String getRuserId() {
        return RuserId;
    }

    public Timestamp getRtime() {
        return Rtime;
    }
}
