package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Group;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GroupInfo {

    public long id;
    public String name;
    public long version;

    public GroupInfo() {
    }

    public GroupInfo(Group group) {
        id = group.getId();
        name = group.getName();
        version = group.getVersion();
    }

}
