package com.example.newstep.Models;

import com.google.firebase.Timestamp;

import java.util.List;

public class ChatroomModel {
    String chatroomId,lastMsgSenderId,lastMsgSent;
    int unseenMsg;
    List<String> userIds;
    com.google.firebase.Timestamp lastMsgTimeStamp;
    public ChatroomModel(){}

    public ChatroomModel(String chatroomId, List<String> userIds, Timestamp lastMsgTimeStamp, String lastMsgSenderId, String lastMsgSent, int unseenMsg) {
        this.chatroomId = chatroomId;
        this.lastMsgSenderId = lastMsgSenderId;
        this.userIds = userIds;
        this.unseenMsg=unseenMsg;
        this.lastMsgTimeStamp = lastMsgTimeStamp;
        this.lastMsgSent=lastMsgSent;
    }

    public String getChatroomId() {
        return chatroomId;
    }

    public int getUnseenMsg() {
        return unseenMsg;
    }

    public void setUnseenMsg(int unseenMsg) {
        this.unseenMsg = unseenMsg;
    }

    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public String getLastMsgSenderId() {
        return lastMsgSenderId;
    }

    public void setLastMsgSenderId(String lastMsgSenderId) {
        this.lastMsgSenderId = lastMsgSenderId;
    }

    public String getLastMsgSent() {
        return lastMsgSent;
    }

    public void setLastMsgSent(String lastMsgSent) {
        this.lastMsgSent = lastMsgSent;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public com.google.firebase.Timestamp getLastMsgTimeStamp() {
        return lastMsgTimeStamp;
    }

    public void setLastMsgTimeStamp(Timestamp lastMsgTimeStamp) {
        this.lastMsgTimeStamp = lastMsgTimeStamp;
    }
}

