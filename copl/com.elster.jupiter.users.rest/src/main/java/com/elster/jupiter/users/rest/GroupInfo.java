package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class GroupInfo {

    public long id;
    public String name;
    public long version;
    public List<PrivilegeInfo> privileges = new ArrayList<>();

    public GroupInfo() {
    }

    public GroupInfo(Group group) {
        id = group.getId();
        name = group.getName();
        version = group.getVersion();
        for (Privilege privilege : group.getPrivileges()) {
            privileges.add(new PrivilegeInfo(privilege));
        }
    }

    
}
