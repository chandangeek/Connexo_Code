package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.users.User;


public class SimplifiedUserInfo {

    public long id;
    public String name;
    public String description;
    public Boolean active;
    public String domain;

    public SimplifiedUserInfo(){

    }

    public SimplifiedUserInfo(User user){
        id = user.getId();
        name = user.getName();
        description = user.getDescription();
        active = user.getStatus();
        domain = user.getDomain();
    }
}
