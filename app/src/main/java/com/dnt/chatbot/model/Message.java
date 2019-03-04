package com.dnt.chatbot.model;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

/**
 * Created by sir.dnt@gmail.com on 3/2/19.
 */
public class Message implements IMessage {

    private String id;
    private User user;
    private String text;
    private Date createdAt;

    public Message(String id, User user, String text, Date createdAt) {
        this.id = id;
        this.user = user;
        this.text = text;
        this.createdAt = createdAt;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public IUser getUser() {
        return this.user;
    }

    @Override
    public Date getCreatedAt() {
        return this.createdAt;
    }
}
