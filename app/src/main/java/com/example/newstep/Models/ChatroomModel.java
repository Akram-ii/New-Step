package com.example.newstep.Models;

import com.google.firebase.Timestamp;

import java.util.List;

public class ChatroomModel {
    String chatroomId,lastMsgSenderId,lastMsgSent,ownerId,groupName,desc,privacy,icon,iconColor;
    int unseenMsg,number_members,isGroup;
    List<String> userIds;
    com.google.firebase.Timestamp lastMsgTimeStamp;
    public ChatroomModel(){}

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIconColor() {
        return iconColor;
    }

    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public ChatroomModel(String chatroomId, List<String> userIds, Timestamp lastMsgTimeStamp, String lastMsgSenderId, String lastMsgSent, int unseenMsg, int number_members, int isGroup, String ownerId, String groupName, String desc, String privacy) {
        this.chatroomId = chatroomId;
        this.lastMsgSenderId = lastMsgSenderId;
        this.userIds = userIds;
        this.unseenMsg=unseenMsg;
        this.lastMsgTimeStamp = lastMsgTimeStamp;
        this.lastMsgSent=lastMsgSent;
    }



    public int getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(int isGroup) {
        this.isGroup = isGroup;
    }

    public int getNumber_members() {
        return number_members;
    }

    public void setNumber_members(int number_members) {
        this.number_members = number_members;
    }

    public boolean checkUser(String userId){
    if (userIds.contains(userId)) {
        return true;
    }
    return false;
}
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

