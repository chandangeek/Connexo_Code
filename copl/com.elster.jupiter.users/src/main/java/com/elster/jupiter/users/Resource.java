package com.elster.jupiter.users;

import com.elster.jupiter.util.HasName;

import java.util.List;

/*
 * represents a collection of privileges
 */
public interface Resource extends HasName {
    String getComponentName();
    String getDescription();
    void delete();
    void createPrivilege(String name);
    List<Privilege> getPrivileges();


}
