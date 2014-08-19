package com.elster.jupiter.users.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Resources{

    USERS("user.users", "user.users.description", getUserPrivileges()),
    GROUPS("group.groups", "group.groups.description", getGroupPrivileges()),
    DOMAINS("domain.domains", "domain.domains.description", getDomainPrivileges());

    private String name;
    private String description;
    private List<String> privileges;

    private Resources(String name, String description, List<String> privileges){
        this.name = name;
        this.description = description;
        this.privileges = privileges;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPrivileges() {
        return privileges;
    }

    private final static List<String> getUserPrivileges() {
        List<String> privileges = new ArrayList<>();
        privileges.add(Privileges.UPDATE_USER);
        privileges.add(Privileges.VIEW_USER);
        return privileges;
    }

    private final static List<String> getGroupPrivileges() {
        List<String> privileges = new ArrayList<>();
        privileges.add(Privileges.CREATE_GROUP);
        privileges.add(Privileges.UPDATE_GROUP);
        privileges.add(Privileges.DELETE_GROUP);
        privileges.add(Privileges.VIEW_GROUP);
        return privileges;
    }

    private final static List<String> getDomainPrivileges() {
        List<String> privileges = new ArrayList<>();
        privileges.add(Privileges.CREATE_DOMAIN);
        privileges.add(Privileges.UPDATE_DOMAIN);
        privileges.add(Privileges.DELETE_DOMAIN);
        privileges.add(Privileges.VIEW_DOMAIN);
        return privileges;
    }
}
