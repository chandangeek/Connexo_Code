package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserInGroupInfo {
    public long userId;
    public GroupInfo groupInfo;

    public UserInGroupInfo(NlsService nlsService, User user, Group group) {
        this();
        userId = user.getId();
        groupInfo = new GroupInfo(nlsService, group);
    }

    public UserInGroupInfo() {
    }

}