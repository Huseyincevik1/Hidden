package com.hbcevik.hiddenproject;

import java.util.Date;

public class Comment {
    public String commentId;
    public String userId;
    public String content1;
    public String postId;
    private Date timestamp;

    public Comment() {

    }

    public Comment(String userId, String content, String postId,Date timestamp) {
        this.userId = userId;
        this.content1 = content;
        this.postId = postId;
        this.timestamp = timestamp;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent1() {
        return content1;
    }

    public void setContent1(String content1) {
        this.content1 = content1;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Date getTimestamp() {return timestamp;}

    public void setTimestamp(Date timestamp) {this.timestamp = timestamp;}
}
