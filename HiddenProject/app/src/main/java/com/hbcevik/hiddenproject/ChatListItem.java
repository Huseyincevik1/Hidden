package com.hbcevik.hiddenproject;

import java.util.Date;

public class ChatListItem {
    private String receiverId;
    private String lastMessageContent;
    private Date lastMessageTimestamp;

    public ChatListItem(String receiverId, String lastMessageContent, Date lastMessageTimestamp) {
        this.receiverId = receiverId;
        this.lastMessageContent = lastMessageContent;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public Date getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }
}
