package com.connectify.connectify.models;

public class ChatRoom {
    private String chatId;
    private String chatPartnerId;
    private String chatPartnerName;
    private String chatPartnerProfile;
    private String chatPartnerEmail;
    private String lastMessage;
    private long timestamp;
    private int unreadCount;

    public ChatRoom() {}

    public ChatRoom(String chatPartnerId, String chatPartnerName, String chatPartnerProfile, String chatPartnerEmail, String lastMessage, long timestamp, int unreadCount) {
        this.chatPartnerId = chatPartnerId;
        this.chatPartnerName = chatPartnerName;
        this.chatPartnerProfile = chatPartnerProfile;
        this.chatPartnerEmail = chatPartnerEmail;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
    }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getChatPartnerId() { return chatPartnerId; }
    public void setChatPartnerId(String chatPartnerId) { this.chatPartnerId = chatPartnerId; }

    public String getChatPartnerName() { return chatPartnerName; }
    public void setChatPartnerName(String chatPartnerName) { this.chatPartnerName = chatPartnerName; }

    public String getChatPartnerProfile() { return chatPartnerProfile; }
    public void setChatPartnerProfile(String chatPartnerProfile) { this.chatPartnerProfile = chatPartnerProfile; }

    public String getChatPartnerEmail() { return chatPartnerEmail; }
    public void setChatPartnerEmail(String chatPartnerEmail) { this.chatPartnerEmail = chatPartnerEmail; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
