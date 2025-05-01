package com.example.newstep.Models;

public class MessageAI {
    public static String SENT_BY_ME="me";
    public static String SENT_BY_BOT="bot";
    private String message;
    private String sentBy;

    public MessageAI(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }

    public static String getSentByMe() {
        return SENT_BY_ME;
    }

    public static void setSentByMe(String sentByMe) {
        SENT_BY_ME = sentByMe;
    }

    public static String getSentByBot() {
        return SENT_BY_BOT;
    }

    public static void setSentByBot(String sentByBot) {
        SENT_BY_BOT = sentByBot;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }
}

