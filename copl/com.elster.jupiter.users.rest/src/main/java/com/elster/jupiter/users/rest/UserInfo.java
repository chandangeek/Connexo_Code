package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;

import javax.xml.bind.annotation.XmlRootElement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement
public class UserInfo {

    public long id;
    public String authenticationName;
    public String description;
    public long version;
    public String domain;
    public String language;
    public String createdOn;
    public String modifiedOn;
    public List<GroupInfo> groups = new ArrayList<>();

    public UserInfo() {
    }

    public UserInfo(User user) {
        id = user.getId();
        authenticationName = user.getName();
        description = user.getDescription();
        version = user.getVersion();
        domain=user.getDomain();
        language=user.getLanguage();
        createdOn=DateFormat.getDateTimeInstance().format(user.getCreationDate());
        modifiedOn=DateFormat.getDateTimeInstance().format(user.getModifiedDate());
        for (Group group : user.getGroups()) {
            groups.add(new GroupInfo(group));
        }
    }

    public void update(User user) {
        user.setDescription(description);
    }
}
