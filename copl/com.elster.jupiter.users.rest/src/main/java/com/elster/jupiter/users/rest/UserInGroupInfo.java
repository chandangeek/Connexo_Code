package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserInGroupInfo {
    public long userId;
    public GroupInfo groupInfo;

    public UserInGroupInfo(Thesaurus thesaurus, User user, Group group) {
        userId = user.getId();
        groupInfo = new GroupInfo(thesaurus, group);
    }

    public UserInGroupInfo() {
    }
}
