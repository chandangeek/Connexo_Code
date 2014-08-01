package com.elster.jupiter.users.security;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public enum Resources {
    USERS("user.users", "user.users.description", UserPrivileges.getValues()),
    GROUPS("group.groups", "group.groups.description", GroupPrivileges.getValues()),
    DOMAINS("domain.domains", "domain.domains.description", DomainPrivileges.getValues());

    private String value;
    private String description;
    private HashMap<Long, String> privileges;

    private Resources(String value, String description, HashMap<Long, String> privileges){
        this.value = value;
        this.description = description;
        this.privileges = privileges;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public HashMap<Long, String> getPrivilegeValues() {
        return privileges;
    }
}
