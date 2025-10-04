package com.connectify.connectify.models;

public class ChatUser {
    private String userEmail;
    private String chatId;

    public ChatUser(String userEmail, String chatId) {
        this.userEmail = userEmail;
        this.chatId = chatId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getChatId() {
        return chatId;
    }
}
