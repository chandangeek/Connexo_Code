package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

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
        domain = user.getDomain();
        language = user.getLanguage();
        createdOn = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(user.getCreationDate().atZone(ZoneId.systemDefault()));
        modifiedOn = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(user.getModifiedDate().atZone(ZoneId.systemDefault()));
        for (Group group : user.getGroups()) {
            groups.add(new GroupInfo(group));
        }

        Collections.sort(groups, new Comparator<GroupInfo>() {
            public int compare(GroupInfo g1, GroupInfo g2) {
                return g1.name.compareTo(g2.name);
            }
        });
    }

    public boolean update(User user) {
        if (description != null && !description.equals(user.getDescription())) {
            user.setDescription(description);
            return true;
        }
        return false;
    }
}
