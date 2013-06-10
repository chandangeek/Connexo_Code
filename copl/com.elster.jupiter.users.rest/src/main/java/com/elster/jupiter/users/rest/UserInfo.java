package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.User;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserInfo {

    public long id;
    public String authenticationName;
    public String description;
    public long version;

    public UserInfo() {
    }

    public UserInfo(User user) {
        id = user.getId();
        authenticationName = user.getName();
        description = user.getDescription();
        version = user.getVersion();
    }

    public void update(User user) {
        user.setDescription(description);
    }
}
