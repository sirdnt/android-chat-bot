package com.dnt.chatbot.model;

import com.stfalcon.chatkit.commons.models.IUser;

/**
 * Created by sir.dnt@gmail.com on 3/2/19.
 */
public class User implements IUser {

    private String id;
    private String name;
    private String avatar;
    private boolean online;

    public User(String id, String name, String avatar, boolean online) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.online = online;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getAvatar() {
        return this.avatar;
    }

}
