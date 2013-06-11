package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class UserInfo {

    public long id;
    public String authenticationName;
    public String description;
    public long version;
    public List<GroupInfo> groups = new ArrayList<>();

    public UserInfo() {
    }

    public UserInfo(User user) {
        id = user.getId();
        authenticationName = user.getName();
        description = user.getDescription();
        version = user.getVersion();
        for (Group group : user.getGroups()) {
            groups.add(new GroupInfo(group));
        }
    }

    public void update(User user) {
        user.setDescription(description);
    }
}
